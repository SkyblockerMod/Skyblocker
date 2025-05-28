package de.hysky.skyblocker.skyblock.item.custom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CustomArmorAnimatedDyesTest {
	@Test
	void testInterpolate() {
		CustomArmorAnimatedDyes.AnimatedDye animatedDye = new CustomArmorAnimatedDyes.AnimatedDye(List.of(new CustomArmorAnimatedDyes.Keyframe(0xFF0000, 0), new CustomArmorAnimatedDyes.Keyframe(0x0000FF, 1)), true, 0, 1);
		CustomArmorAnimatedDyes.AnimatedDyeStateTracker tracker = new CustomArmorAnimatedDyes.AnimatedDyeStateTracker(animatedDye);
		// Expected values at 0, 0.25, 0.5, 0.75, and 1 progress
		// See https://observablehq.com/@aras-p/oklab-interpolation-test for an online interpolation tool
		Assertions.assertEquals(0xFE0000, tracker.interpolate(animatedDye, 0));
		Assertions.assertEquals(0xC5496C, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0x8C53A2, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0x5047D1, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0x0000FE, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0x5047D1, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0x8C53A2, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0xC5496C, tracker.interpolate(animatedDye, 5));
		Assertions.assertEquals(0xFE0000, tracker.interpolate(animatedDye, 5));
	}
}
