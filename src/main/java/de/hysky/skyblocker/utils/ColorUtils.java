package de.hysky.skyblocker.utils;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;

public class ColorUtils {
	/**
	 * Takes an RGB color as an integer and returns an array of the color's components as floats, in RGB format.
	 *
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
		return getFloatComponents(dye.getEntityColor());
	}

	/**
	 * Interpolates linearly between two colours.
	 */
	//Credit to https://codepen.io/OliverBalfour/post/programmatically-making-gradients
	public static int interpolate(int firstColor, int secondColor, double percentage) {
		int r1 = MathHelper.square((firstColor >> 16) & 0xFF);
		int g1 = MathHelper.square((firstColor >> 8) & 0xFF);
		int b1 = MathHelper.square(firstColor & 0xFF);

		int r2 = MathHelper.square((secondColor >> 16) & 0xFF);
		int g2 = MathHelper.square((secondColor >> 8) & 0xFF);
		int b2 = MathHelper.square(secondColor & 0xFF);

		double inverse = 1d - percentage;

		int r3 = (int) Math.floor(Math.sqrt(r1 * inverse + r2 * percentage));
		int g3 = (int) Math.floor(Math.sqrt(g1 * inverse + g2 * percentage));
		int b3 = (int) Math.floor(Math.sqrt(b1 * inverse + b2 * percentage));

		return (r3 << 16) | (g3 << 8 ) | b3;
	}
}
