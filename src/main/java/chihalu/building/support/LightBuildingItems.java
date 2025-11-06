package chihalu.building.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.LightBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * 光源建材のリストを管理するユーティリティ。
 */
public final class LightBuildingItems {
	private static final List<ItemStack> LIGHT_BLOCKS = buildLightBlockList();

	private LightBuildingItems() {
	}

	public static void populate(ItemGroup.Entries entries) {
		for (ItemStack stack : LIGHT_BLOCKS) {
			entries.add(stack.copy(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	public static ItemStack getIconStack() {
		return new ItemStack(Items.SEA_LANTERN);
	}

	private static List<ItemStack> buildLightBlockList() {
		List<ItemStack> stacks = new ArrayList<>();

		addSet(stacks,
			Items.TORCH,
			Items.SOUL_TORCH,
			Items.REDSTONE_TORCH,
			getItemOrNull("copper_torch"));
		addSet(stacks,
			Items.LANTERN,
			Items.SOUL_LANTERN);
		addItemsById(stacks,
			"copper_lantern",
			"exposed_copper_lantern",
			"weathered_copper_lantern",
			"oxidized_copper_lantern",
			"waxed_copper_lantern",
			"waxed_exposed_copper_lantern",
			"waxed_weathered_copper_lantern",
			"waxed_oxidized_copper_lantern");
		addSet(stacks, Items.CANDLE, Items.WHITE_CANDLE, Items.ORANGE_CANDLE, Items.MAGENTA_CANDLE, Items.LIGHT_BLUE_CANDLE,
			Items.YELLOW_CANDLE, Items.LIME_CANDLE, Items.PINK_CANDLE, Items.GRAY_CANDLE, Items.LIGHT_GRAY_CANDLE,
			Items.CYAN_CANDLE, Items.PURPLE_CANDLE, Items.BLUE_CANDLE, Items.BROWN_CANDLE, Items.GREEN_CANDLE,
			Items.RED_CANDLE, Items.BLACK_CANDLE);
		addSet(stacks, Items.GLOWSTONE, Items.SEA_LANTERN, Items.SHROOMLIGHT, Items.GLOW_LICHEN);
		addSet(stacks, Items.END_ROD, Items.BEACON, Items.CONDUIT);
		addSet(stacks, Items.JACK_O_LANTERN, Items.REDSTONE_LAMP, Items.OCHRE_FROGLIGHT, Items.VERDANT_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT);
		addSet(stacks, Items.CAMPFIRE, Items.SOUL_CAMPFIRE);
		addSet(stacks, Items.MAGMA_BLOCK);
		addItemsById(stacks, "end_gateway", "end_portal", "nether_portal");
		addSet(stacks, Items.AMETHYST_CLUSTER, Items.LARGE_AMETHYST_BUD, Items.MEDIUM_AMETHYST_BUD, Items.SMALL_AMETHYST_BUD);
		addLightLevelStacks(stacks);

		return Collections.unmodifiableList(stacks);
	}

	private static void addSet(List<ItemStack> stacks, Item... items) {
		for (Item item : items) {
			if (item == null || item == Items.AIR) {
				continue;
			}
			stacks.add(new ItemStack(item));
		}
	}

	private static void addItemsById(List<ItemStack> stacks, String... ids) {
		for (String id : ids) {
			Item item = getItemOrNull(id);
			if (item != null) {
				stacks.add(new ItemStack(item));
			}
		}
	}

	private static Item getItemOrNull(String id) {
		Identifier identifier = Identifier.ofVanilla(id);
		if (!Registries.ITEM.containsId(identifier)) {
			return null;
		}
		Item item = Registries.ITEM.get(identifier);
		return item == Items.AIR ? null : item;
	}

	private static void addLightLevelStacks(List<ItemStack> stacks) {
		for (int level = 0; level <= 15; level++) {
			ItemStack stack = new ItemStack(Items.LIGHT);
			LightBlock.addNbtForLevel(stack, level);
			stacks.add(stack);
		}
	}
}
