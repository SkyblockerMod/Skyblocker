package de.hysky.skyblocker.skyblock.item;


import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;

public class ConsumableProtection {
	@Init
	public static void init() {
		UseItemCallback.EVENT.register(ConsumableProtection::onInteract);
		//Prevents placing the items when they are player heads (counts for consuming them)
		UseBlockCallback.EVENT.register((player, world, hand, _) -> onInteract(player, world, hand));
	}

	private static InteractionResult onInteract(Player player, Level world, InteractionHand hand) {
		if (world.isClientSide() && SkyblockerConfigManager.get().general.itemProtection.protectValuableConsumables && Utils.isOnSkyblock()) {
			ItemStack stack = player.getItemInHand(hand);
			String skyblockId = stack.getSkyblockId();

			if (!skyblockId.isEmpty() && Constants.EVOLVING_CONSUMABLES.contains(skyblockId)) {
				return InteractionResult.FAIL;
			}
		}

		return InteractionResult.PASS;
	}
}
