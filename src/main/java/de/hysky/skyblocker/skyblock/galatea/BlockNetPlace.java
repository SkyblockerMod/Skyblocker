package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class BlockNetPlace {

	@Init
	public static void init() {
		UseBlockCallback.EVENT.register(BlockNetPlace::onBlockPlace);
	}

	private static ActionResult onBlockPlace(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (!SkyblockerConfigManager.get().foraging.galatea.disableFishingNetPlacement) return ActionResult.PASS;
		ItemStack heldItem = player.getStackInHand(hand);

		if (heldItem.getItem() == Items.COBWEB && heldItem.getName().getString().contains("Fishing Net")) {
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}
}
