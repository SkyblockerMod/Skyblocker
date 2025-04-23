package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class IslandPickobulusBlock {

	@Init
	public static void init() {
		UseItemCallback.EVENT.register((player, world, hand) ->
				IslandPickobulusBlock.checkForPickobulus(player, hand));
		UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) ->
				IslandPickobulusBlock.checkForPickobulus(player, hand));
	}

	private static ActionResult checkForPickobulus(PlayerEntity player, Hand hand) {
		var config = SkyblockerConfigManager.get();
		if (config.mining.islandPickobulusBlock
				&& Utils.getLocation() == Location.PRIVATE_ISLAND) {
			var stack = player.getStackInHand(hand);
			var ability = ItemUtils.getAbility(stack);
			if (ability != null) {
				if (ability.equalsIgnoreCase("pickobulus")) {
					return ActionResult.FAIL; // Cancels and doesn't send a package
				}
			}
		}
		return ActionResult.PASS;
	}
}
