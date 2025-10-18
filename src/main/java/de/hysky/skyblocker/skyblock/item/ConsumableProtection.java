package de.hysky.skyblocker.skyblock.item;

import java.util.Set;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ConsumableProtection {
	private static final Set<String> PROTECTED_CONSUMABLES = Set.of("NEW_BOTTLE_OF_JYRRE", "DARK_CACAO_TRUFFLE", "DISCRITE");

	@Init
	public static void init() {
		UseItemCallback.EVENT.register(ConsumableProtection::onInteract);
		//Prevents placing the items when they are player heads (counts for consuming them)
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onInteract(player, world, hand));
	}

	private static ActionResult onInteract(PlayerEntity player, World world, Hand hand) {
		if (world.isClient() && SkyblockerConfigManager.get().general.itemProtection.protectValuableConsumables && Utils.isOnSkyblock()) {
			ItemStack stack = player.getStackInHand(hand);
			String skyblockId = stack.getSkyblockId();

			if (!skyblockId.isEmpty() && PROTECTED_CONSUMABLES.contains(skyblockId)) {
				return ActionResult.FAIL;
			}
		}

		return ActionResult.PASS;
	}
}
