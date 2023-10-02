package me.xmrvizzy.skyblocker.skyblock;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class ItemProtection {
	
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(ItemProtection::registerCommand);
	}

	public static boolean isItemProtected(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		
		NbtCompound nbt = stack.getNbt();
		
		if (nbt != null && nbt.contains("ExtraAttributes")) {
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : "";
			
			return SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid);
		}
		
		return false;
	}
	
	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("protectItem")
						.executes(context -> protectMyItem(context.getSource()))));
	}
	
	private static int protectMyItem(FabricClientCommandSource source) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();
		NbtCompound nbt = (heldItem != null) ? heldItem.getNbt() : null;
		
		if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes")) {
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;
			
			if (itemUuid != null) {
				ObjectOpenHashSet<String> protectedItems = SkyblockerConfigManager.get().general.protectedItems;
				
				if (!protectedItems.contains(itemUuid)) {
					protectedItems.add(itemUuid);
					SkyblockerConfigManager.save();
					
					source.sendFeedback(Text.translatable("skyblocker.itemProtection.added", heldItem.getName()));
				} else {
					protectedItems.remove(itemUuid);
					SkyblockerConfigManager.save();
					
					source.sendFeedback(Text.translatable("skyblocker.itemProtection.removed", heldItem.getName()));
				}
			} else {
				source.sendFeedback(Text.translatable("skyblocker.itemProtection.noItemUuid"));
			}
		} else {
			source.sendFeedback(Text.translatable("skyblocker.itemProtection.unableToProtect"));
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
