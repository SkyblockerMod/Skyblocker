package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import java.util.regex.Pattern;

public class SwingAnimation {

	public static final Pattern ABILITY = Pattern.compile("^ability:\\s.*?\\s+right\\sclick$");

	@Init
	public static void init() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			var config = SkyblockerConfigManager.get();
			var stack = player.getStackInHand(hand);
			if (config.uiAndVisuals.swingOnAbilities
					&& SwingAnimation.hasAbility(stack))
				return ActionResult.SUCCESS;
			return ActionResult.PASS;
		});
	}

	public static boolean hasAbility(ItemStack stack) {
		if (stack.isEmpty()) return false;
		var lore = ItemUtils.getLore(stack);
		for (var line : lore) {
			if (ABILITY.matcher(line.getString().trim().toLowerCase()).matches())
				return true;
		}
		return false;
	}
}
