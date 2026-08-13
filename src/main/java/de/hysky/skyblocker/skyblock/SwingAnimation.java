package de.hysky.skyblocker.skyblock;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.utils.ItemAbility;

public class SwingAnimation {
	public static boolean hasAbility(ItemStack stack) {
		List<ItemAbility> abilities = stack.skyblocker$getAbilities();
		for (ItemAbility ability : abilities) {
			if (ability.activation().isRightClick()) return true;
		}
		return false;
	}
}
