package chihalu.building.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class HistoryManager {
	private static final HistoryManager INSTANCE = new HistoryManager();
	private static final int MAX_HISTORY = 64;
	private static final String DEFAULT_WORLD_KEY = "unknown_world";
	private static final String ALL_WORLD_FILE_NAME = "all_world.json";

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Path historyDir = FabricLoader.getInstance()
		.getGameDir()
		.resolve("itemgroups")
		.resolve("history");

	private final Deque<Identifier> recentItems = new ArrayDeque<>();
	private Path activeHistoryPath = getWorldHistoryPath(DEFAULT_WORLD_KEY);

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
			stacks.add(new ItemStack(Items.LARGE_AMETHYST_BUD));
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
		return new ItemStack(Items.LARGE_AMETHYST_BUD);
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

	private Deque<Identifier> loadHistory(Path path) {
		Deque<Identifier> deque = new ArrayDeque<>();
		if (!Files.exists(path)) {
			return deque;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			SerializableData data = gson.fromJson(reader, SerializableData.class);
			if (data == null || data.items == null) {
				return deque;
			}
			List<String> items = data.items;
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
		} catch (IOException | JsonSyntaxException exception) {
			BuildingSupport.LOGGER.error("Failed to read history data: {}", path, exception);
		}
		return deque;
	}

	private void saveHistory(Path path, Deque<Identifier> deque) {
		try {
			Files.createDirectories(historyDir);
			SerializableData data = new SerializableData(deque.stream().map(Identifier::toString).toList());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				gson.toJson(data, writer);
			}
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("Failed to save history data: {}", path, exception);
		}
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

	private static final class SerializableData {
		private List<String> items;

		private SerializableData(List<String> items) {
			this.items = items;
		}
	}
}
