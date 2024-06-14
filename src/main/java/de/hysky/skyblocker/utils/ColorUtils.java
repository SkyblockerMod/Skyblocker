package de.hysky.skyblocker.utils;

import net.minecraft.util.DyeColor;

public class ColorUtils {
	/**
	 * Takes an RGB color as an integer and returns an array of the color's components as floats, in RGB format.
	 * @param color The color to get the components of.
	 * @return An array of the color's components as floats.
	 */
	public static float[] getFloatComponents(int color) {
		return new float[] {
				((color >> 16) & 0xFF) / 255f,
				((color >> 8) & 0xFF) / 255f,
				(color & 0xFF) / 255f
		};
	}

	/**
	 * @param dye The dye from which the entity color will be used for the components.
	 */
	public static float[] getFloatComponents(DyeColor dye) {
		return new float[] {
				((dye.getEntityColor() >> 16) & 0xFF) / 255f,
				((dye.getEntityColor() >> 8) & 0xFF) / 255f,
				(dye.getEntityColor() & 0xFF) / 255f
		};
	}
}
