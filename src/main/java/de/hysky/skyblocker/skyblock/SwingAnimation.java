package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;

import java.util.Locale;
import java.util.regex.Pattern;

public class SwingAnimation {
	private static final Pattern ABILITY = Pattern.compile("^(⦾\\s)?ability:\\s.*?right\\sclick$");

	public static boolean hasAbility(ItemStack stack) {
		if (stack.isEmpty()) return false;
		var lore = ItemUtils.getLore(stack);
		for (var line : lore) {
			if (ABILITY.matcher(line.getString().trim().toLowerCase(Locale.ENGLISH)).matches())
				return true;
		}
		return false;
	}
}
