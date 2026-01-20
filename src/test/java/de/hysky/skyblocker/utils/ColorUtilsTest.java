package de.hysky.skyblocker.utils;

import net.minecraft.util.ARGB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorUtilsTest {
	@Test
	void testFloatComponents() {
		Assertions.assertArrayEquals(new float[]{0.2f, 0.4f, 0.6f}, ColorUtils.getFloatComponents(ARGB.color(51, 102, 153)));
	}

	@Test
	void testInterpolate() {
		Assertions.assertEquals(0x00FE00, ColorUtils.interpolate(0.5, 0xFF0000, 0x00FF00, 0x0000FF));
		Assertions.assertEquals(0xD0A800, ColorUtils.interpolate(0.25, 0xFF0000, 0x00FF00, 0x0000FF));
		Assertions.assertEquals(0x00A9BE, ColorUtils.interpolate(0.75, 0xFF0000, 0x00FF00, 0x0000FF));
	}
}
