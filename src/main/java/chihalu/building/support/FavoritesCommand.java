package chihalu.building.support;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public final class FavoritesCommand {
	private FavoritesCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, FavoritesManager manager) {
		dispatcher.register(CommandManager.literal("favorites")
			.requires(source -> source.hasPermissionLevel(0))
			.then(CommandManager.literal("add")
				.then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
					.executes(context -> addFavorite(context.getSource(), manager, ItemStackArgumentType.getItemStackArgument(context, "item")))))
			.then(CommandManager.literal("remove")
				.then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
					.executes(context -> removeFavorite(context.getSource(), manager, ItemStackArgumentType.getItemStackArgument(context, "item")))))
			.then(CommandManager.literal("toggle")
				.then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
					.executes(context -> toggleFavorite(context.getSource(), manager, ItemStackArgumentType.getItemStackArgument(context, "item")))))
			.then(CommandManager.literal("list")
				.executes(context -> listFavorites(context.getSource(), manager)))
			.then(CommandManager.literal("clear")
				.executes(context -> clearFavorites(context.getSource(), manager))));
	}

	private static int addFavorite(ServerCommandSource source, FavoritesManager manager, ItemStackArgument argument) throws CommandSyntaxException {
		ItemStack stack = argument.createStack(1, false);
		Identifier id = Registries.ITEM.getId(stack.getItem());

		boolean added = manager.addFavorite(id);
		Text itemText = stack.toHoverableText();

		if (added) {
			source.sendFeedback(() -> Text.translatable("command.building-support.favorite.added", itemText), false);
			return 1;
		}

		source.sendFeedback(() -> Text.translatable("command.building-support.favorite.exists", itemText), false);
		return 0;
	}

	private static int removeFavorite(ServerCommandSource source, FavoritesManager manager, ItemStackArgument argument) throws CommandSyntaxException {
		ItemStack stack = argument.createStack(1, false);
		Identifier id = Registries.ITEM.getId(stack.getItem());

		boolean removed = manager.removeFavorite(id);
		Text itemText = stack.toHoverableText();

		if (removed) {
			source.sendFeedback(() -> Text.translatable("command.building-support.favorite.removed", itemText), false);
			return 1;
		}

		source.sendFeedback(() -> Text.translatable("command.building-support.favorite.missing", itemText), false);
		return 0;
	}

	private static int toggleFavorite(ServerCommandSource source, FavoritesManager manager, ItemStackArgument argument) throws CommandSyntaxException {
		ItemStack stack = argument.createStack(1, false);
		Identifier id = Registries.ITEM.getId(stack.getItem());
		boolean added = manager.toggleFavorite(id);
		Text itemText = stack.toHoverableText();

		if (added) {
			source.sendFeedback(() -> Text.translatable("command.building-support.favorite.added", itemText), false);
			return 1;
		}

		source.sendFeedback(() -> Text.translatable("command.building-support.favorite.removed", itemText), false);
		return 1;
	}

	private static int listFavorites(ServerCommandSource source, FavoritesManager manager) {
		List<ItemStack> stacks = manager.getFavoriteStacks();

		if (stacks.isEmpty()) {
			source.sendFeedback(() -> Text.translatable("command.building-support.favorite.list.empty"), false);
			return 0;
		}

		MutableText contents = Text.empty();

		for (int i = 0; i < stacks.size(); i++) {
			if (i > 0) {
				contents.append(Text.literal(", ").formatted(Formatting.GRAY));
			}

			contents.append(stacks.get(i).toHoverableText());
		}

		source.sendFeedback(() -> Text.translatable("command.building-support.favorite.list", contents), false);
		return stacks.size();
	}

	private static int clearFavorites(ServerCommandSource source, FavoritesManager manager) {
		manager.clearFavorites();
		source.sendFeedback(() -> Text.translatable("command.building-support.favorite.cleared"), false);
		return 1;
	}
}
