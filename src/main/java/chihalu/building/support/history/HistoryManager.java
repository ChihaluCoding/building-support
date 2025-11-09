package chihalu.building.support.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import chihalu.building.support.BuildingSupport;
import chihalu.building.support.config.BuildingSupportConfig;
import chihalu.building.support.BuildingSupportStorage;

public final class HistoryManager {
	private static final HistoryManager INSTANCE = new HistoryManager();
	private static final int MAX_HISTORY = 64;
	private static final String DEFAULT_WORLD_KEY = "unknown_world";
	private static final String ALL_WORLD_FILE_NAME = "all_world.json";

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Path historyDir = BuildingSupportStorage.resolve("history");

	private final Deque<Identifier> recentItems = new ArrayDeque<>();
	private Path activeHistoryPath = getWorldHistoryPath(DEFAULT_WORLD_KEY);
	private String activeWorldKey = DEFAULT_WORLD_KEY;

	private HistoryManager() {
	}

	public static HistoryManager getInstance() {
		return INSTANCE;
	}

	public synchronized void initialize() {
		try {
			Files.createDirectories(historyDir);
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("Failed to prepare history directory: {}", historyDir, exception);
		}
		setActiveWorldKey(null);
	}

	public synchronized void setActiveWorldKey(String worldKey) {
		String sanitized = sanitize(worldKey);
		activeHistoryPath = getWorldHistoryPath(sanitized);
		this.activeWorldKey = worldKey == null || worldKey.isBlank() ? DEFAULT_WORLD_KEY : worldKey;
		reloadActive();
	}

	public synchronized void reloadActive() {
		recentItems.clear();
		recentItems.addAll(loadHistory(activeHistoryPath));
	}

	public synchronized void recordUsage(Identifier id) {
		if (id == null || !Registries.ITEM.containsId(id)) {
			return;
		}

		updateDeque(recentItems, id);
		saveHistory(activeHistoryPath, recentItems);
		updateGlobalHistory(id);
	}

	public synchronized List<ItemStack> getDisplayStacksForTab() {
		Deque<Identifier> source = getHistoryIdentifiersForDisplay();
		List<ItemStack> stacks = new ArrayList<>();
		for (Identifier id : source) {
			ItemStack stack = createStack(id);
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}
		if (stacks.isEmpty()) {
			stacks.add(new ItemStack(Items.BOOK));
		}
		return stacks;
	}

	public synchronized ItemStack getIconStack() {
		Deque<Identifier> source = getHistoryIdentifiersForDisplay();
		for (Identifier id : source) {
			ItemStack stack = createStack(id);
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return new ItemStack(Items.BOOK);
	}

	public synchronized void populate(ItemGroup.Entries entries) {
		for (ItemStack stack : getDisplayStacksForTab()) {
			entries.add(stack.copy(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	private Deque<Identifier> getHistoryIdentifiersForDisplay() {
		BuildingSupportConfig.HistoryDisplayMode mode = BuildingSupportConfig.getInstance().getHistoryDisplayMode();
		if (mode == BuildingSupportConfig.HistoryDisplayMode.ALL_WORLD) {
			return loadHistory(getGlobalHistoryPath());
		}
		return new ArrayDeque<>(recentItems);
	}

	private void updateGlobalHistory(Identifier id) {
		Path globalPath = getGlobalHistoryPath();
		Deque<Identifier> global = loadHistory(globalPath);
		updateDeque(global, id);
		saveHistory(globalPath, global);
	}

	public synchronized boolean resetHistory(Path historyPath) {
		boolean deleted = deleteHistoryFile(historyPath);
		if (deleted && historyPath.equals(activeHistoryPath)) {
			reloadActive();
		}
		return deleted;
	}

	public synchronized boolean resetActiveWorldHistory() {
		boolean deleted = deleteHistoryFile(activeHistoryPath);
		if (deleted) {
			reloadActive();
		}
		return deleted;
	}

	public synchronized boolean resetGlobalHistory() {
		// 共有履歴ファイルとワールド別履歴ファイルを両方削除してリセットとみなす
		boolean deletedGlobal = deleteHistoryFile(getGlobalHistoryPath());
		boolean deletedWorlds = deleteWorldHistoryFiles();
		return deletedGlobal || deletedWorlds;
	}

	// 履歴ディレクトリ内からワールド別履歴ファイルを列挙し、すべて削除する
	private boolean deleteWorldHistoryFiles() {
		if (!Files.exists(historyDir)) {
			return false;
		}

		boolean deletedAny = false;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(historyDir, "*.json")) {
			for (Path path : stream) {
				if (ALL_WORLD_FILE_NAME.equals(path.getFileName().toString())) {
					continue;
				}
				if (deleteHistoryFile(path)) {
					deletedAny = true;
				}
			}
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("Failed to delete world-specific history files: {}", historyDir, exception);
		}
		return deletedAny;
	}

	private boolean deleteHistoryFile(Path path) {
		try {
			Files.deleteIfExists(path);
			return true;
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("Failed to delete history data: {}", path, exception);
			return false;
		}
	}

	public synchronized List<WorldHistoryInfo> listWorldHistories() {
		List<WorldHistoryInfo> entries = new ArrayList<>();
		if (!Files.exists(historyDir)) {
			return entries;
		}

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(historyDir, "*.json")) {
			for (Path path : stream) {
				if (Files.isDirectory(path)) {
					continue;
				}
				String fileName = path.getFileName().toString();
				if (ALL_WORLD_FILE_NAME.equals(fileName)) {
					continue;
				}
				String fallback = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
				String displayName = readSerializableData(path)
					.map(data -> data.worldKey)
					.filter(key -> key != null && !key.isBlank())
					.orElse(fallback);
				entries.add(new WorldHistoryInfo(path, displayName, fallback));
			}
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("Failed to scan history directory: {}", historyDir, exception);
		}

		entries.sort(Comparator.comparing(WorldHistoryInfo::displayName, String.CASE_INSENSITIVE_ORDER));
		return entries;
	}

	private Deque<Identifier> loadHistory(Path path) {
		Deque<Identifier> deque = new ArrayDeque<>();
		Optional<SerializableData> data = readSerializableData(path);
		if (data.isEmpty() || data.get().items == null) {
			return deque;
		}
		List<String> items = data.get().items;
		for (int i = items.size() - 1; i >= 0; i--) {
			String idString = items.get(i);
			if (idString == null || idString.isBlank()) {
				continue;
			}
			Identifier id = Identifier.tryParse(idString.trim());
			if (id != null && Registries.ITEM.containsId(id)) {
				updateDeque(deque, id);
			}
		}
		return deque;
	}

	private void saveHistory(Path path, Deque<Identifier> deque) {
		try {
			Files.createDirectories(historyDir);
			SerializableData data = new SerializableData(deque.stream().map(Identifier::toString).toList(), activeWorldKey);
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				gson.toJson(data, writer);
			}
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("Failed to save history data: {}", path, exception);
		}
	}

	private Optional<SerializableData> readSerializableData(Path path) {
		if (!Files.exists(path)) {
			return Optional.empty();
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			SerializableData data = gson.fromJson(reader, SerializableData.class);
			return Optional.ofNullable(data);
		} catch (IOException | JsonSyntaxException exception) {
			BuildingSupport.LOGGER.error("Failed to read history data: {}", path, exception);
		}
		return Optional.empty();
	}

	private static void updateDeque(Deque<Identifier> deque, Identifier id) {
		deque.remove(id);
		deque.addFirst(id);
		while (deque.size() > MAX_HISTORY) {
			deque.removeLast();
		}
	}

	private ItemStack createStack(Identifier id) {
		if (!Registries.ITEM.containsId(id)) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(Registries.ITEM.get(id));
	}

	private Path getWorldHistoryPath(String sanitizedKey) {
		return historyDir.resolve(sanitizedKey + ".json");
	}

	private Path getGlobalHistoryPath() {
		return historyDir.resolve(ALL_WORLD_FILE_NAME);
	}

	private static String sanitize(String key) {
		if (key == null || key.isBlank()) {
			return DEFAULT_WORLD_KEY;
		}
		String sanitized = key.replaceAll("[^a-zA-Z0-9._-]", "_");
		if (sanitized.isBlank()) {
			return DEFAULT_WORLD_KEY;
		}
		if (sanitized.length() > 80) {
			sanitized = sanitized.substring(0, 80);
		}
		String hash = Integer.toUnsignedString(key.hashCode());
		return sanitized + "_" + hash;
	}

	public static final class WorldHistoryInfo {
		private final Path path;
		private final String displayName;
		private final String fileName;

		private WorldHistoryInfo(Path path, String displayName, String fileName) {
			this.path = path;
			this.displayName = displayName;
			this.fileName = fileName;
		}

		public Path path() {
			return path;
		}

		public String displayName() {
			return displayName;
		}

		public String fileName() {
			return fileName;
		}
	}

	private static final class SerializableData {
		private List<String> items;
		private String worldKey;

		private SerializableData(List<String> items, String worldKey) {
			this.items = items;
			this.worldKey = worldKey;
		}
	}
}
