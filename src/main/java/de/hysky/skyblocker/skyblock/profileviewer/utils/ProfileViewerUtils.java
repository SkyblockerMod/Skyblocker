package de.hysky.skyblocker.skyblock.profileviewer.utils;

import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;

public class ProfileViewerUtils {
	public static FlexibleItemStack createSkull(String textureB64) {
		return ItemUtils.createSkull(textureB64);
	}

	public static String numLetterFormat(double amount) {
		if (amount >= 1_000_000_000) {
			return String.format("%.4gB", amount / 1_000_000_000);
		} else if (amount >= 1_000_000) {
			return String.format("%.4gM", amount / 1_000_000);
		} else if (amount >= 1_000) {
			return String.format("%.4gK", amount / 1_000);
		} else {
			return String.valueOf((int) amount);
		}
	}
}
