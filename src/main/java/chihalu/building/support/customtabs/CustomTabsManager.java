package chihalu.building.support.customtabs;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import chihalu.building.support.BuildingSupport;
import chihalu.building.support.BuildingSupportStorage;
import chihalu.building.support.config.BuildingSupportConfig;
import chihalu.building.support.client.accessor.ItemGroupIconAccessor;

/**
 * カスタムタブに登録されたアイテムを管理するクラス。
 */
public final class CustomTabsManager {
	public static final CustomTabsManager INSTANCE = new CustomTabsManager();

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Path configPath = BuildingSupportStorage.resolve("custom_tabs.json");

	private final LinkedHashSet<Identifier> items = new LinkedHashSet<>();
	private ItemGroup registeredGroup;

	private CustomTabsManager() {
	}

	public static CustomTabsManager getInstance() {
		return INSTANCE;
	}

	public synchronized void reload() {
		items.clear();
		if (!Files.exists(configPath)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
			SerializableData data = gson.fromJson(reader, SerializableData.class);
			if (data == null || data.items == null) {
				return;
			}
			for (String rawId : data.items) {
				if (rawId == null || rawId.isBlank()) {
					continue;
				}
				Identifier id = Identifier.tryParse(rawId.trim());
				if (!isValidItem(id)) {
					BuildingSupport.LOGGER.warn("無効なカスタムタブ用IDを検出しました: {}", rawId);
					continue;
				}
				items.add(id);
			}
		} catch (IOException | JsonSyntaxException exception) {
			BuildingSupport.LOGGER.error("custom_tabs.json の読み込みに失敗しました: {}", configPath, exception);
		}
	}

	public synchronized void registerGroupInstance(ItemGroup group) {
		this.registeredGroup = group;
	}

	public synchronized boolean isCustomTabGroup(ItemGroup group) {
		return registeredGroup == group;
	}

	public synchronized boolean addItem(Identifier id) {
		if (!isValidItem(id)) {
			return false;
		}
		boolean added = items.add(id);
		if (added) {
			save();
		}
		return added;
	}

	public synchronized boolean removeItem(Identifier id) {
		boolean removed = items.remove(id);
		if (removed) {
			save();
		}
		return removed;
	}

	public synchronized boolean toggleItem(Identifier id) {
		if (!isValidItem(id)) {
			return false;
		}
		boolean added;
		if (items.contains(id)) {
			items.remove(id);
			added = false;
		} else {
			items.add(id);
			added = true;
		}
		save();
		return added;
	}

	public synchronized void clear() {
		if (items.isEmpty()) {
			return;
		}
		items.clear();
		save();
	}

	public synchronized List<Identifier> getItems() {
		return List.copyOf(items);
	}

	public synchronized ItemStack getIconStack() {
		ItemStack configured = BuildingSupportConfig.getInstance().getCustomTabIconStack();
		if (!configured.isEmpty()) {
			return configured.copy();
		}
		List<ItemStack> stacks = getDisplayStacks();
		return stacks.isEmpty() ? new ItemStack(Items.PAPER) : stacks.get(0);
	}

	public synchronized List<ItemStack> getDisplayStacksForTab() {
		return getDisplayStacks();
	}

	private synchronized List<ItemStack> getDisplayStacks() {
		List<ItemStack> stacks = new ArrayList<>();
		for (Identifier id : items) {
			if (!Registries.ITEM.containsId(id)) {
				continue;
			}
			stacks.add(new ItemStack(Registries.ITEM.get(id)));
		}
		if (stacks.isEmpty()) {
			stacks.add(new ItemStack(Items.PAPER));
		}
		return List.copyOf(stacks);
	}

	public synchronized void populateEntries(ItemGroup.Entries entries) {
		for (ItemStack stack : getDisplayStacks()) {
			entries.add(stack.copy(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	public synchronized void refreshGroupIcon() {
		if (registeredGroup instanceof ItemGroupIconAccessor accessor) {
			accessor.utility_toolkit$resetIconCache();
			registeredGroup.getIcon();
		}
	}

	private boolean isValidItem(Identifier id) {
		return id != null && Registries.ITEM.containsId(id);
	}

	private synchronized void save() {
		SerializableData data = new SerializableData(items.stream().map(Identifier::toString).toList());
		try {
			Files.createDirectories(configPath.getParent());
			try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
				gson.toJson(data, writer);
			}
		} catch (IOException exception) {
			BuildingSupport.LOGGER.error("custom_tabs.json の保存に失敗しました: {}", configPath, exception);
		}
	}

	private static final class SerializableData {
		private List<String> items;

		private SerializableData() {
		}

		private SerializableData(List<String> items) {
			this.items = items;
		}
	}
}
