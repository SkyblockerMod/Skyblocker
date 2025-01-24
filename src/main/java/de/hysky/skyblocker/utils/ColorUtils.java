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
	 * @param pcnt Percentage between 0% and 100%, NOT 0-1!
	 * @return an int representing a color, where 100% = green and 0% = red
	 */
	public static int percentToColor(float pcnt) {
		return MathHelper.hsvToRgb(pcnt / 300, 1, 1);
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

	/**
	 * Checks if the specified color is bright or dark.
	 */
	public static boolean isBright(int color) {
		float[] components = getFloatComponents(color);

		float red = components[0];
		float green = components[1];
		float blue = components[2];

		double luminance = luminance(red, green, blue);
		double whiteContrast = contrastRatio(luminance, 1.0);
		double blackContrast = contrastRatio(luminance, 0.0);
		return whiteContrast < blackContrast;
	}

	/**
	 * Returns the luminance value of the color.
	 *
	 * @link <a href="https://stackoverflow.com/questions/596216/formula-to-determine-perceived-brightness-of-rgb-color/56678483#56678483">Stackoverflow explanation</a>
	 */
	private static double luminance(float red, float green, float blue) {
		double r = (red <= 0.04045F) ? red / 12.92F : Math.pow((red + 0.055F) / 1.055F, 2.4F);
		double g = (green <= 0.04045F) ? green / 12.92F : Math.pow((green + 0.055F) / 1.055F, 2.4F);
		double b = (blue <= 0.04045F) ? blue / 12.92F : Math.pow((blue + 0.055F) / 1.055F, 2.4F);

		return Math.fma(0.2126, r, Math.fma(0.7152, g, 0.0722 * b));
	}

	/**
	 * Checks the contrast ratio between two luminance values.
	 */
	private static double contrastRatio(double background, double content) {
		double brightest = Math.max(background, content);
		double darkest = Math.min(background, content);

		return (brightest + 0.05) / (darkest + 0.05);
	}


}
