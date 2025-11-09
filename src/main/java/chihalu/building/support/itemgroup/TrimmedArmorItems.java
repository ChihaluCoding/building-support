package chihalu.building.support.itemgroup;

import java.util.List;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * 建築支援タブで使うトリム付き防具を一括生成するユーティリティです。
 * すべての模様パターンと素材の組み合わせを事前に展開し、タブに素早く登録できるようにします。
 */
public final class TrimmedArmorItems {
	private TrimmedArmorItems() {
	}

	public static void populateLeather(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
		populate(context.lookup(), entries, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
	}

	public static void populateChainmail(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
		populate(context.lookup(), entries, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
	}

	public static void populateIron(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
		populate(context.lookup(), entries, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
	}

	public static void populateGold(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
		populate(context.lookup(), entries, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
	}

	public static void populateDiamond(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
		populate(context.lookup(), entries, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
	}

	public static void populateNetherite(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
		populate(context.lookup(), entries, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
	}

	public static ItemStack createIcon(Item iconArmor) {
		RegistryWrapper.WrapperLookup lookup = BuiltinRegistries.createWrapperLookup();
		List<RegistryEntry.Reference<ArmorTrimPattern>> patterns = getPatterns(lookup);
		List<RegistryEntry.Reference<ArmorTrimMaterial>> materials = getMaterials(lookup);
		ItemStack stack = new ItemStack(iconArmor);
		if (!patterns.isEmpty() && !materials.isEmpty()) {
			stack.set(DataComponentTypes.TRIM, new ArmorTrim(materials.getFirst(), patterns.getFirst()));
		}
		return stack;
	}

	private static void populate(RegistryWrapper.WrapperLookup lookup, ItemGroup.Entries entries, Item... armorPieces) {
		List<RegistryEntry.Reference<ArmorTrimPattern>> patterns = getPatterns(lookup);
		List<RegistryEntry.Reference<ArmorTrimMaterial>> materials = getMaterials(lookup);

		if (patterns.isEmpty() || materials.isEmpty()) {
			// パターンまたは素材のレジストリが空であれば、無加工の防具だけを登録する
			for (Item armorPiece : armorPieces) {
				entries.add(new ItemStack(armorPiece), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
			}
			return;
		}

		for (RegistryEntry<ArmorTrimPattern> pattern : patterns) {
			for (RegistryEntry<ArmorTrimMaterial> material : materials) {
				for (Item armorPiece : armorPieces) {
					entries.add(createTrimmedStack(armorPiece, pattern, material), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
				}
			}
		}
	}

	private static ItemStack createTrimmedStack(Item armorPiece, RegistryEntry<ArmorTrimPattern> pattern, RegistryEntry<ArmorTrimMaterial> material) {
		ItemStack stack = new ItemStack(armorPiece);
		stack.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
		return stack;
	}

	private static List<RegistryEntry.Reference<ArmorTrimPattern>> getPatterns(RegistryWrapper.WrapperLookup lookup) {
		// getOrThrowでレジストリを取得し、不整合があれば即座に例外化して原因を表面化させる
		return lookup.getOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries().toList();
	}

	private static List<RegistryEntry.Reference<ArmorTrimMaterial>> getMaterials(RegistryWrapper.WrapperLookup lookup) {
		// トリム素材も同様に参照し、空であれば呼び出し元のフォールバックに任せる
		return lookup.getOrThrow(RegistryKeys.TRIM_MATERIAL).streamEntries().toList();
	}
}

