package de.hysky.skyblocker.skyblock.item;

import java.util.Set;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConsumableProtection {
	private static final Set<String> PROTECTED_CONSUMABLES = Set.of("NEW_BOTTLE_OF_JYRRE", "DARK_CACAO_TRUFFLE", "DISCRITE", "MOBY_DUCK", "ROSEWATER_FLASK");

	@Init
	public static void init() {
		UseItemCallback.EVENT.register(ConsumableProtection::onInteract);
		//Prevents placing the items when they are player heads (counts for consuming them)
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onInteract(player, world, hand));
	}

	private static InteractionResult onInteract(Player player, Level world, InteractionHand hand) {
		if (world.isClientSide() && SkyblockerConfigManager.get().general.itemProtection.protectValuableConsumables && Utils.isOnSkyblock()) {
			ItemStack stack = player.getItemInHand(hand);
			String skyblockId = stack.getSkyblockId();

			if (!skyblockId.isEmpty() && PROTECTED_CONSUMABLES.contains(skyblockId)) {
				return InteractionResult.FAIL;
			}
		}

		return InteractionResult.PASS;
	}
}
