package de.hysky.skyblocker.skyblock;

import java.util.List;

import de.hysky.skyblocker.utils.ItemAbility;
import net.minecraft.world.item.ItemStack;

public class SwingAnimation {
	public static boolean hasAbility(ItemStack stack) {
		List<ItemAbility> abilities = ItemAbility.getAbilities(stack);
		if (abilities.isEmpty()) return false;
		for (ItemAbility ability : abilities) {
			if (ability.activation() == ItemAbility.Activation.RIGHT_CLICK) return true;
		}
		return false;
	}
}
