package chihalu.building.support;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import chihalu.building.support.mixin.client.CreativeInventoryScreenInvoker;
import chihalu.building.support.mixin.client.HandledScreenAccessor;

public class BuildingSupportClient implements ClientModInitializer {
	private static final KeyBinding.Category FAVORITE_CATEGORY = KeyBinding.Category.create(BuildingSupport.id("favorites"));
	private KeyBinding toggleFavoriteKey;

	@Override
	public void onInitializeClient() {
		toggleFavoriteKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.building-support.add_favorite",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_B,
			FAVORITE_CATEGORY
		));

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) ->
			ScreenKeyboardEvents.beforeKeyPress(screen).register(new ScreenKeyboardEvents.BeforeKeyPress() {
				@Override
				public void beforeKeyPress(net.minecraft.client.gui.screen.Screen currentScreen, KeyInput input) {
					if (!toggleFavoriteKey.matchesKey(input)) {
						return;
					}

					if (!(currentScreen instanceof CreativeInventoryScreen creativeScreen)) {
						if (client.player != null) {
							client.player.sendMessage(Text.translatable("message.building-support.favorite.require_creative"), false);
						}
						return;
					}

					handleToggleFavorite(client, creativeScreen);
				}
			})
		);
	}

	private void handleToggleFavorite(MinecraftClient client, CreativeInventoryScreen screen) {
		Slot slot = ((HandledScreenAccessor) screen).building_support$getFocusedSlot();
		if (slot == null || !slot.hasStack()) {
			client.player.sendMessage(Text.translatable("message.building-support.favorite.no_item"), false);
			return;
		}

		ItemStack stack = slot.getStack();
		Identifier id = Registries.ITEM.getId(stack.getItem());
		FavoritesManager manager = FavoritesManager.getInstance();
		boolean added = manager.toggleFavorite(id);
		Text itemText = stack.toHoverableText();

		if (added) {
			client.player.sendMessage(Text.translatable("message.building-support.favorite.added", itemText).formatted(Formatting.GREEN), false);
		} else {
			client.player.sendMessage(Text.translatable("message.building-support.favorite.removed", itemText).formatted(Formatting.YELLOW), false);
		}

		ItemGroup favoritesGroup = Registries.ITEM_GROUP.get(BuildingSupport.FAVORITES_ITEM_GROUP_KEY);
		var favoritesStacks = manager.getDisplayStacksForTab();

		var displayStacks = favoritesGroup.getDisplayStacks();
		displayStacks.clear();
		displayStacks.addAll(favoritesStacks);

		var searchStacks = favoritesGroup.getSearchTabStacks();
		searchStacks.clear();
		searchStacks.addAll(favoritesStacks);

		ItemGroup currentTab = CreativeInventoryScreenInvoker.building_support$getSelectedTab();

		if (currentTab == favoritesGroup) {
			((CreativeInventoryScreenInvoker) screen).building_support$refreshSelectedTab(favoritesStacks);
		}
	}
}
