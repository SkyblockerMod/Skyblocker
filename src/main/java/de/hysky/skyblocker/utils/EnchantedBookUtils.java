package de.hysky.skyblocker.utils;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class EnchantedBookUtils {
	public static String getApiIdByName(Component enchantName) {
		String name = enchantName.getString().toUpperCase(Locale.ENGLISH);
		name = name.replace("BUY ", "").replace("SELL ", "").replace("'", "");

		String[] parts = name.split(" ");
		if (parts.length == 0) return "";
		if (RomanNumerals.isValidRomanNumeral(parts[parts.length - 1])) {
			parts[parts.length - 1] = String.valueOf(RomanNumerals.romanToDecimal(parts[parts.length - 1]));
		}
		boolean isUltimate = !enchantName.getSiblings().isEmpty() && enchantName.getSiblings().getLast().getStyle().isBold();
		return "ENCHANTMENT_" + (isUltimate ? "ULTIMATE_" : "") + String.join("_", parts);
	}
}
