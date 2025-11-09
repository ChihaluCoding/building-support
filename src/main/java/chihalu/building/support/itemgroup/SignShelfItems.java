package chihalu.building.support.itemgroup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * 看板・吊り看板・棚・本棚をまとめて扱うクリエイティブタブ用ユーティリティ。
 */
public final class SignShelfItems {
	private static final List<Item> SHELF_VARIANTS = List.of(
		Items.OAK_SHELF,
		Items.SPRUCE_SHELF,
		Items.BIRCH_SHELF,
		Items.JUNGLE_SHELF,
		Items.ACACIA_SHELF,
		Items.DARK_OAK_SHELF,
		Items.MANGROVE_SHELF,
		Items.PALE_OAK_SHELF,
		Items.BAMBOO_SHELF,
		Items.CHERRY_SHELF,
		Items.CRIMSON_SHELF,
		Items.WARPED_SHELF
	);

	private static final List<Item> SIGN_VARIANTS = List.of(
		Items.OAK_SIGN,
		Items.SPRUCE_SIGN,
		Items.BIRCH_SIGN,
		Items.JUNGLE_SIGN,
		Items.ACACIA_SIGN,
		Items.DARK_OAK_SIGN,
		Items.MANGROVE_SIGN,
		Items.PALE_OAK_SIGN,
		Items.BAMBOO_SIGN,
		Items.CHERRY_SIGN,
		Items.CRIMSON_SIGN,
		Items.WARPED_SIGN
	);

	private static final List<Item> HANGING_SIGN_VARIANTS = List.of(
		Items.OAK_HANGING_SIGN,
		Items.SPRUCE_HANGING_SIGN,
		Items.BIRCH_HANGING_SIGN,
		Items.JUNGLE_HANGING_SIGN,
		Items.ACACIA_HANGING_SIGN,
		Items.DARK_OAK_HANGING_SIGN,
		Items.MANGROVE_HANGING_SIGN,
		Items.PALE_OAK_HANGING_SIGN,
		Items.BAMBOO_HANGING_SIGN,
		Items.CHERRY_HANGING_SIGN,
		Items.CRIMSON_HANGING_SIGN,
		Items.WARPED_HANGING_SIGN
	);

	private static final List<ItemStack> ENTRIES = List.copyOf(createEntries());

	private SignShelfItems() {
	}

	public static void populate(ItemGroup.Entries entries) {
		for (ItemStack stack : ENTRIES) {
			entries.add(stack.copy(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	public static ItemStack getIconStack() {
		return ENTRIES.get(0).copy();
	}

	private static List<ItemStack> createEntries() {
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(new ItemStack(Items.BOOKSHELF));
		stacks.add(new ItemStack(Items.CHISELED_BOOKSHELF));
		addStacks(stacks, SHELF_VARIANTS);
		addStacks(stacks, SIGN_VARIANTS);
		addStacks(stacks, HANGING_SIGN_VARIANTS);
		return stacks;
	}

	private static void addStacks(List<ItemStack> stacks, List<Item> items) {
		for (Item item : items) {
			stacks.add(new ItemStack(item));
		}
	}
}
