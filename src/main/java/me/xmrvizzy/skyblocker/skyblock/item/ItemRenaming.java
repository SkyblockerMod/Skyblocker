package me.xmrvizzy.skyblocker.skyblock.item;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class ItemRenaming {

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(ItemRenaming::registerCommands);
	}
	
	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("renameItem")
						.executes(context -> renameItem(context.getSource(), null))
						.then(ClientCommandManager.argument("textComponent", TextArgumentType.text())
								.executes(context -> renameItem(context.getSource(), context.getArgument("textComponent", Text.class))))));
	}
	
	private static int renameItem(FabricClientCommandSource source, Text text) {
		MinecraftClient client = source.getClient();
		ItemStack heldItem = client.player.getMainHandStack();
		NbtCompound nbt = (heldItem != null) ? heldItem.getNbt() : null;
		
		if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes")) {
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid =  extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;
			
			if (itemUuid != null) {
				Map<String, Text> customItemNames = SkyblockerConfig.get().general.customItemNames;
				
				if (text == null) {
					//Remove custom item name when the text argument isn't passed
					customItemNames.remove(itemUuid);
					SkyblockerConfig.save();
					source.sendFeedback(Text.translatable("skyblocker.customItemNames.removed"));
				} else {
					//If the text is provided then set the item's custom name to it
					customItemNames.put(itemUuid, text);
					SkyblockerConfig.save();
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
