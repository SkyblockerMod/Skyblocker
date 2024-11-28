package de.hysky.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
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
								.executes(context -> renameItem(context.getSource(), null))
								.then(ClientCommandManager.argument("textComponent", TextArgumentType.text(registryAccess))
										.executes(context -> renameItem(context.getSource(), context.getArgument("textComponent", Text.class)))))));
	}

	@SuppressWarnings("SameReturnValue")
	private static int renameItem(FabricClientCommandSource source, Text text) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(source.getPlayer().getMainHandStack());

			if (!itemUuid.isEmpty()) {
				Object2ObjectOpenHashMap<String, Text> customItemNames = SkyblockerConfigManager.get().general.customItemNames;

				if (text == null) {
					if (customItemNames.containsKey(itemUuid)) {
						//Remove custom item name when the text argument isn't passed
						customItemNames.remove(itemUuid);
						SkyblockerConfigManager.save();
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.removed")));
					} else {
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.neverHad")));
					}
				} else {
					//If the text is provided then set the item's custom name to it

					//Set italic to false if it hasn't been changed (or was already false)
					Style currentStyle = text.getStyle();
					((MutableText) text).setStyle(currentStyle.withItalic(currentStyle.isItalic()));

					customItemNames.put(itemUuid, text);
					SkyblockerConfigManager.save();
					source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.added")));
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.noItemUuid")));
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.unableToSetName")));
		}

		return Command.SINGLE_SUCCESS;
	}
}
