package de.hysky.skyblocker.skyblock;

import java.util.List;

import de.hysky.skyblocker.utils.ItemAbility;
import net.minecraft.world.item.ItemStack;

public class SwingAnimation {
	public static boolean hasAbility(ItemStack stack) {
		List<ItemAbility> abilities = stack.skyblocker$getAbilities();
		for (ItemAbility ability : abilities) {
			if (ability.activation().isRightClick()) return true;
		}
		return false;
	}
}
