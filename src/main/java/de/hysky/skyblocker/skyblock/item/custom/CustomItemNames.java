package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.CustomizeNameScreen;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class CustomItemNames {
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CustomItemNames::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("renameItem")
								.executes(context -> openScreen(context.getSource()))
								.then(ClientCommandManager.argument("textComponent", TextArgumentType.text(registryAccess))
										.executes(context -> renameItem(context.getSource(), context.getArgument("textComponent", Text.class))))
								// greedy string will only consume the arg if the text component parsing fails.
								.then(ClientCommandManager.argument("basicText", StringArgumentType.greedyString())
										.executes(context -> renameItem(context.getSource(), Text.of(context.getArgument("basicText", String.class))))))));
	}

	private static int openScreen(FabricClientCommandSource source) {
		if (!Utils.isOnSkyblock()) {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.notOnSkyblock")));
			return 0;
		}
		ItemStack handStack = source.getPlayer().getMainHandStack();
		if (handStack.isEmpty()) {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.noItem")));
			return 0;
		}
		if (ItemUtils.getItemUuid(handStack).isEmpty()) {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.noItemUuid")));
			return 0;
		}
		Scheduler.queueOpenScreen(new CustomizeNameScreen(handStack));
		return Command.SINGLE_SUCCESS;
	}

	@SuppressWarnings("SameReturnValue")
	private static int renameItem(FabricClientCommandSource source, Text text) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(source.getPlayer().getMainHandStack());

			if (!itemUuid.isEmpty()) {
				SkyblockerConfigManager.update(config -> {
					Object2ObjectOpenHashMap<String, Text> customItemNames = config.general.customItemNames;
					//If the text is provided then set the item's custom name to it

					//Set italic to false if it hasn't been changed (or was already false)
					Style currentStyle = text.getStyle();
					((MutableText) text).setStyle(currentStyle.withItalic(currentStyle.isItalic()));

					customItemNames.put(itemUuid, text);
				});
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.added")));

			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.noItemUuid")));
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.notOnSkyblock")));
		}

		return Command.SINGLE_SUCCESS;
	}
}
