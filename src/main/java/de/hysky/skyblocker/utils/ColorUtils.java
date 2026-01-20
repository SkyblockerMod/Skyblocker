package de.hysky.skyblocker.utils;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public class ColorUtils {
	/**
	 * Takes an RGB color as an integer and returns an array of the color's components as floats, in RGB format.
	 *
	 * @param color The color to get the components of.
	 * @return An array of the color's components as floats.
	 */
	public static float[] getFloatComponents(int color) {
		return new float[] {
				ARGB.redFloat(color),
				ARGB.greenFloat(color),
				ARGB.blueFloat(color),
		};
	}

	/**
	 * @param dye The dye from which the entity color will be used for the components.
	 */
	public static float[] getFloatComponents(DyeColor dye) {
		return getFloatComponents(dye.getTextureDiffuseColor());
	}

	/**
	 * @param pcnt Percentage between 0% and 100%, NOT 0-1!
	 * @return an int representing a color, where 100% = green and 0% = red
	 */
	public static int percentToColor(float pcnt) {
		return Mth.hsvToRgb(pcnt / 300, 1, 1);
	}

	/**
	 * Interpolates between two colours.
	 */
	public static int interpolate(int firstColor, int secondColor, double percentage) {
		return OkLabColor.interpolate(firstColor, secondColor, (float) percentage);
	}

	/**
	 * Interpolates between multiple colors.
	 *
	 * @param percentage percentage between 0 and 1
	 * @param colors     the colors to interpolate between
	 * @return the interpolated color
	 * @see #interpolate(int, int, double)
	 */
	public static int interpolate(double percentage, int... colors) {
		int colorCount = colors.length;
		if (colorCount == 0) {
			return 0;
		}
		if (colorCount == 1 || percentage <= 0) {
			return colors[0];
		}
		if (percentage >= 1) {
			return colors[colorCount - 1];
		}

		double scaledPercentage = percentage * (colorCount - 1);
		int index = (int) Math.floor(scaledPercentage);
		double remainder = scaledPercentage - index;

		return interpolate(colors[index], colors[index + 1], remainder);
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
