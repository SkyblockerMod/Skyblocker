package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.screen.CustomizeScreen;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class CustomItemNames {
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CustomItemNames::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("renameItem")
								.executes(context -> openScreen(context.getSource()))
								.then(ClientCommandManager.argument("textComponent", ComponentArgument.textComponent(registryAccess))
										.executes(context -> renameItem(context.getSource(), context.getArgument("textComponent", Component.class))))
								// greedy string will only consume the arg if the text component parsing fails.
								.then(ClientCommandManager.argument("basicText", StringArgumentType.greedyString())
										.executes(context -> renameItem(context.getSource(), Component.nullToEmpty(context.getArgument("basicText", String.class))))))));
	}

	private static int openScreen(FabricClientCommandSource source) {
		if (!Utils.isOnSkyblock()) {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customItemNames.notOnSkyblock")));
			return 0;
		}
		ItemStack handStack = source.getPlayer().getMainHandItem();
		if (handStack.isEmpty()) {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customItemNames.noItem")));
			return 0;
		}
		if (handStack.getUuid().isEmpty()) {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customItemNames.noItemUuid")));
			return 0;
		}
		Scheduler.queueOpenScreen(new CustomizeScreen(null, true));
		return Command.SINGLE_SUCCESS;
	}

	@SuppressWarnings("SameReturnValue")
	private static int renameItem(FabricClientCommandSource source, Component text) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = source.getPlayer().getMainHandItem().getUuid();

			if (!itemUuid.isEmpty()) {
				SkyblockerConfigManager.update(config -> {
					Object2ObjectOpenHashMap<String, Component> customItemNames = config.general.customItemNames;
					//If the text is provided then set the item's custom name to it

					//Set italic to false if it hasn't been changed (or was already false)
					Style currentStyle = text.getStyle();
					((MutableComponent) text).setStyle(currentStyle.withItalic(currentStyle.isItalic()));

					customItemNames.put(itemUuid, text);
				});
				source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customItemNames.added")));

			} else {
				source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customItemNames.noItemUuid")));
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customItemNames.notOnSkyblock")));
		}

		return Command.SINGLE_SUCCESS;
	}
}
