package de.hysky.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class CustomItemNames {
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CustomItemNames::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("renameItem")
								.executes(context -> renameItem(context.getSource(), null))
								.then(ClientCommandManager.argument("textComponent", TextArgumentType.text())
										.executes(context -> renameItem(context.getSource(), context.getArgument("textComponent", Text.class)))))));
	}

	@SuppressWarnings("SameReturnValue")
	private static int renameItem(FabricClientCommandSource source, Text text) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();
		NbtCompound nbt = (heldItem != null) ? heldItem.getNbt() : null;

		if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes")) {
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;

			if (itemUuid != null) {
				Object2ObjectOpenHashMap<String, Text> customItemNames = SkyblockerConfigManager.get().general.customItemNames;

				if (text == null) {
					if (customItemNames.containsKey(itemUuid)) {
						//Remove custom item name when the text argument isn't passed
						customItemNames.remove(itemUuid);
						SkyblockerConfigManager.save();
						source.sendFeedback(Text.translatable("skyblocker.customItemNames.removed"));
					} else {
						source.sendFeedback(Text.translatable("skyblocker.customItemNames.neverHad"));
					}
				} else {
					//If the text is provided then set the item's custom name to it

					//Set italic to false if it hasn't been changed (or was already false)
					Style currentStyle = text.getStyle();
					((MutableText) text).setStyle(currentStyle.withItalic((currentStyle.isItalic() ? true : false)));

					customItemNames.put(itemUuid, text);
					SkyblockerConfigManager.save();
					source.sendFeedback(Text.translatable("skyblocker.customItemNames.added"));
				}
			} else {
				source.sendError(Text.translatable("skyblocker.customItemNames.noItemUuid"));
			}
		} else {
			source.sendError(Text.translatable("skyblocker.customItemNames.unableToSetName"));
		}

		return Command.SINGLE_SUCCESS;
	}
}
