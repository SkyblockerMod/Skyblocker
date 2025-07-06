package de.hysky.skyblocker.utils;

/**
 * Implements color interpolation in the OkLab color space.
 *
 * @see <a href="https://bottosson.github.io/posts/oklab">OkLab Colour Space</a>
 * @see <a href="https://www.sjbrown.co.uk/posts/gamma-correct-rendering/">Gamma Correct Rendering</a>
 */
@SuppressWarnings("UnaryPlus")
public class OkLabColor {

	/**
	 * Converts a linear SRGB color to the OkLab color space.
	 *
	 * @param r the linearized red channel
	 * @param g the linearized green channel
	 * @param b the linearized blue channel
	 */
	private static Lab linearSRGB2OkLab(float r, float g, float b) {
		float l = Math.fma(0.4122214708f, r, Math.fma(0.5363325363f, g, 0.0514459929f * b));
		float m = Math.fma(0.2119034982f, r, Math.fma(0.6806995451f, g, 0.1073969566f * b));
		float s = Math.fma(0.0883024619f, r, Math.fma(0.2817188376f, g, 0.6299787005f * b));

		float l_ = (float) Math.cbrt(l);
		float m_ = (float) Math.cbrt(m);
		float s_ = (float) Math.cbrt(s);

		float L = Math.fma(0.2104542553f, l_, Math.fma(+0.7936177850f, m_, -0.0040720468f * s_));
		float A = Math.fma(1.9779984951f, l_, Math.fma(-2.4285922050f, m_, +0.4505937099f * s_));
		float B = Math.fma(0.0259040371f, l_, Math.fma(+0.7827717662f, m_, -0.8086757660f * s_));

		return new Lab(L, A, B);
	}

	/**
	 * Converts a color in the OkLab color space to linear SRGB.
	 */
	private static RGB okLab2LinearSRGB(float L, float A, float B) {
		float l_ = L + 0.3963377774f * A + 0.2158037573f * B;
		float m_ = L - 0.1055613458f * A - 0.0638541728f * B;
		float s_ = L - 0.0894841775f * A - 1.2914855480f * B;

		float l = l_ * l_ * l_;
		float m = m_ * m_ * m_;
		float s = s_ * s_ * s_;

		float r = Math.fma(+4.0767416621f, l, Math.fma(-3.3077115913f, m, +0.2309699292f * s));
		float g = Math.fma(-1.2684380046f, l, Math.fma(+2.6097574011f, m, -0.3413193965f * s));
		float b = Math.fma(-0.0041960863f, l, Math.fma(-0.7034186147f, m, +1.7076147010f * s));

		return new RGB(r, g, b);
	}

	/**
	 * Converts {@code channel} from RGB to linear SRGB.
	 */
	private static float linearize(float channel) {
		return channel <= 0.04045f ? channel / 12.92f : (float) Math.pow((channel + 0.055f) / 1.055f, 2.4f);
	}

	/**
	 * Converts {@code channel} from linear SRGB to RGB.
	 */
	private static float delinearize(float channel) {
		return channel <= 0.0031308f ? channel * 12.92f : Math.fma(1.055f, (float) Math.pow(channel, 1.0f / 2.4f), -0.055f);
	}

	/**
	 * Interpolates two colors using the OkLab color space.
	 *
	 * @param firstColor  the RGB color at the left end of the gradient
	 * @param secondColor the RGB color at the right end of the gradient
	 * @param progress    a float from [0, 1] representing the position in the gradient
	 *
	 * @return the interpolated color in the RGB format
	 */
	//Escape analysis should hopefully take care of the objects :')
	public static int interpolate(int firstColor, int secondColor, float progress) {
		//Normalize colours to a range of [0, 1]
		float normalizedR1 = ((firstColor >> 16) & 0xFF) / 255f;
		float normalizedG1 = ((firstColor >> 8) & 0xFF) / 255f;
		float normalizedB1 = (firstColor & 0xFF) / 255f;

		float normalizedR2 = ((secondColor >> 16) & 0xFF) / 255f;
		float normalizedG2 = ((secondColor >> 8) & 0xFF) / 255f;
		float normalizedB2 = (secondColor & 0xFF) / 255f;

		Lab lab1 = linearSRGB2OkLab(linearize(normalizedR1), linearize(normalizedG1), linearize(normalizedB1));
		Lab lab2 = linearSRGB2OkLab(linearize(normalizedR2), linearize(normalizedG2), linearize(normalizedB2));

		float L = Math.fma(progress, (lab2.l - lab1.l), lab1.l);
		float A = Math.fma(progress, (lab2.a - lab1.a), lab1.a);
		float B = Math.fma(progress, (lab2.b - lab1.b), lab1.b);

		RGB rgb = okLab2LinearSRGB(L, A, B);
		int r = Math.clamp((int) (delinearize(rgb.r) * 255f), 0, 255);
		int g = Math.clamp((int) (delinearize(rgb.g) * 255f), 0, 255);
		int b = Math.clamp((int) (delinearize(rgb.b) * 255f), 0, 255);

		return (r << 16) | (g << 8) | b;
	}

	private record Lab(float l, float a, float b) {}
	private record RGB(float r, float g, float b) {}
}
