package de.hysky.skyblocker.skyblock;

import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class SwingAnimation {
	private static final Pattern ABILITY = Pattern.compile("^(⦾\\s)?ability:\\s.*?right\\sclick$");

	public static boolean hasAbility(ItemStack stack) {
		if (stack.isEmpty()) return false;
		var lore = stack.skyblocker$getLoreStrings();
		for (var line : lore) {
			if (ABILITY.matcher(line.trim().toLowerCase(Locale.ENGLISH)).matches())
				return true;
		}
		return false;
	}
}
