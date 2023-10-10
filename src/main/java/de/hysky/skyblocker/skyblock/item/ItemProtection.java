package de.hysky.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemProtection {

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(ItemProtection::registerCommand);
	}

	public static boolean isItemProtected(ItemStack stack) {
		if (stack == null) return false;
        String itemUuid = ItemUtils.getItemUuid(stack);
        return SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid);
	}

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("protectItem")
						.executes(context -> protectMyItem(context.getSource()))));
	}

	private static int protectMyItem(FabricClientCommandSource source) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();

		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(heldItem);

			if (!itemUuid.isEmpty()) {
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
