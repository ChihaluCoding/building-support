package chihalu.building.support.itemgroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * 銅建材のリストを管理するユーティリティ。
 */
public final class CopperBuildingItems {
	private static final List<ItemStack> COPPER_BLOCKS = buildCopperBlockList();

	private CopperBuildingItems() {
	}

	public static void populate(ItemGroup.Entries entries) {
		for (ItemStack stack : COPPER_BLOCKS) {
			entries.add(stack.copy(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	public static ItemStack getIconStack() {
		return new ItemStack(Items.COPPER_BLOCK);
	}

	private static List<ItemStack> buildCopperBlockList() {
		List<ItemStack> stacks = new ArrayList<>();

		stacks.add(new ItemStack(Items.COPPER_ORE));
		stacks.add(new ItemStack(Items.DEEPSLATE_COPPER_ORE));
		stacks.add(new ItemStack(Items.RAW_COPPER_BLOCK));

		// 基本銅ブロックと派生ブロック
		addCopperTier(stacks,
			Items.COPPER_BLOCK, Items.CUT_COPPER, Items.CUT_COPPER_SLAB, Items.CUT_COPPER_STAIRS,
			Items.COPPER_DOOR, Items.COPPER_TRAPDOOR, Items.COPPER_GRATE, Items.COPPER_BULB);
		addCopperTier(stacks,
			Items.EXPOSED_COPPER, Items.EXPOSED_CUT_COPPER, Items.EXPOSED_CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_STAIRS,
			Items.EXPOSED_COPPER_DOOR, Items.EXPOSED_COPPER_TRAPDOOR, Items.EXPOSED_COPPER_GRATE, Items.EXPOSED_COPPER_BULB);
		addCopperTier(stacks,
			Items.WEATHERED_COPPER, Items.WEATHERED_CUT_COPPER, Items.WEATHERED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER_STAIRS,
			Items.WEATHERED_COPPER_DOOR, Items.WEATHERED_COPPER_TRAPDOOR, Items.WEATHERED_COPPER_GRATE, Items.WEATHERED_COPPER_BULB);
		addCopperTier(stacks,
			Items.OXIDIZED_COPPER, Items.OXIDIZED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER_SLAB, Items.OXIDIZED_CUT_COPPER_STAIRS,
			Items.OXIDIZED_COPPER_DOOR, Items.OXIDIZED_COPPER_TRAPDOOR, Items.OXIDIZED_COPPER_GRATE, Items.OXIDIZED_COPPER_BULB);

		// ワックス済みバリエーション
		addCopperTier(stacks,
			Items.WAXED_COPPER_BLOCK, Items.WAXED_CUT_COPPER, Items.WAXED_CUT_COPPER_SLAB, Items.WAXED_CUT_COPPER_STAIRS,
			Items.WAXED_COPPER_DOOR, Items.WAXED_COPPER_TRAPDOOR, Items.WAXED_COPPER_GRATE, Items.WAXED_COPPER_BULB);
		addCopperTier(stacks,
			Items.WAXED_EXPOSED_COPPER, Items.WAXED_EXPOSED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER_SLAB, Items.WAXED_EXPOSED_CUT_COPPER_STAIRS,
			Items.WAXED_EXPOSED_COPPER_DOOR, Items.WAXED_EXPOSED_COPPER_TRAPDOOR, Items.WAXED_EXPOSED_COPPER_GRATE, Items.WAXED_EXPOSED_COPPER_BULB);
		addCopperTier(stacks,
			Items.WAXED_WEATHERED_COPPER, Items.WAXED_WEATHERED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER_SLAB, Items.WAXED_WEATHERED_CUT_COPPER_STAIRS,
			Items.WAXED_WEATHERED_COPPER_DOOR, Items.WAXED_WEATHERED_COPPER_TRAPDOOR, Items.WAXED_WEATHERED_COPPER_GRATE, Items.WAXED_WEATHERED_COPPER_BULB);
		addCopperTier(stacks,
			Items.WAXED_OXIDIZED_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER_SLAB, Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
			Items.WAXED_OXIDIZED_COPPER_DOOR, Items.WAXED_OXIDIZED_COPPER_TRAPDOOR, Items.WAXED_OXIDIZED_COPPER_GRATE, Items.WAXED_OXIDIZED_COPPER_BULB);

		// その他の銅系建材
		addWeatheringSet(stacks, "lightning_rod");
		addWaxedWeatheringSet(stacks, "lightning_rod");
		stacks.add(new ItemStack(Items.CHISELED_COPPER));
		stacks.add(new ItemStack(Items.EXPOSED_CHISELED_COPPER));
		stacks.add(new ItemStack(Items.WEATHERED_CHISELED_COPPER));
		stacks.add(new ItemStack(Items.OXIDIZED_CHISELED_COPPER));
		stacks.add(new ItemStack(Items.WAXED_CHISELED_COPPER));
		stacks.add(new ItemStack(Items.WAXED_EXPOSED_CHISELED_COPPER));
		stacks.add(new ItemStack(Items.WAXED_WEATHERED_CHISELED_COPPER));
		stacks.add(new ItemStack(Items.WAXED_OXIDIZED_CHISELED_COPPER));

		addItemIfPresent(stacks, "chain");
		addItemIfPresent(stacks, "iron_bars");

		addWeatheringSet(stacks, "copper_chain");
		addWeatheringSet(stacks, "copper_lantern");
		addWeatheringSet(stacks, "copper_bars");
		addWeatheringSet(stacks, "copper_torch");

		addWaxedWeatheringSet(stacks, "copper_chain");
		addWaxedWeatheringSet(stacks, "copper_lantern");
		addWaxedWeatheringSet(stacks, "copper_bars");
		addWaxedWeatheringSet(stacks, "copper_torch");

		return Collections.unmodifiableList(stacks);
	}

	private static void addCopperTier(List<ItemStack> stacks,
									  net.minecraft.item.Item block,
									  net.minecraft.item.Item cut,
									  net.minecraft.item.Item slab,
									  net.minecraft.item.Item stairs,
									  net.minecraft.item.Item door,
									  net.minecraft.item.Item trapdoor,
									  net.minecraft.item.Item grate,
									  net.minecraft.item.Item bulb) {
		stacks.add(new ItemStack(block));
		stacks.add(new ItemStack(cut));
		stacks.add(new ItemStack(slab));
		stacks.add(new ItemStack(stairs));
		stacks.add(new ItemStack(door));
		stacks.add(new ItemStack(trapdoor));
		stacks.add(new ItemStack(grate));
		stacks.add(new ItemStack(bulb));
	}

	private static void addWeatheringSet(List<ItemStack> stacks, String baseId) {
		addVariant(stacks, baseId);
		addVariant(stacks, "exposed_" + baseId);
		addVariant(stacks, "weathered_" + baseId);
		addVariant(stacks, "oxidized_" + baseId);
	}

	private static void addWaxedWeatheringSet(List<ItemStack> stacks, String baseId) {
		addVariant(stacks, "waxed_" + baseId);
		addVariant(stacks, "waxed_exposed_" + baseId);
		addVariant(stacks, "waxed_weathered_" + baseId);
		addVariant(stacks, "waxed_oxidized_" + baseId);
	}

	private static void addVariant(List<ItemStack> stacks, String id) {
		addItemIfPresent(stacks, id);
	}

	private static void addItemIfPresent(List<ItemStack> stacks, String id) {
		Identifier identifier = Identifier.ofVanilla(id);
		if (!Registries.ITEM.containsId(identifier)) {
			return;
		}
		Item item = Registries.ITEM.get(identifier);
		if (item != Items.AIR) {
			stacks.add(new ItemStack(item));
		}
	}
}
