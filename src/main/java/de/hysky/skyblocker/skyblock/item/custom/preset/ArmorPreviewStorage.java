package de.hysky.skyblocker.skyblock.item.custom.preset;

import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary data used when rendering armor preset previews.
 */
public final class ArmorPreviewStorage {
	private ArmorPreviewStorage() {}

	public static final Map<String, CustomArmorTrims.ArmorTrimId> TEMP_TRIMS = new HashMap<>();
	public static final Map<String, Integer> TEMP_DYE_COLORS = new HashMap<>();
	public static final Map<String, CustomArmorAnimatedDyes.AnimatedDye> TEMP_ANIMATED_DYES = new HashMap<>();
	public static final Map<String, String> TEMP_HELMET_TEXTURES = new HashMap<>();

	/**
	 * Remove all temporary preview data.
	 */
	public static void clear() {
		TEMP_TRIMS.clear();
		TEMP_DYE_COLORS.clear();
		TEMP_ANIMATED_DYES.clear();
		TEMP_HELMET_TEXTURES.clear();
	}
}
