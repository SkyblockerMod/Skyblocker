package de.hysky.skyblocker.utils.waypoint;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfileAwareWaypointTest {
	@Test
	void testShouldRender() {
		ProfileAwareWaypoint waypoint = new ProfileAwareWaypoint(BlockPos.ORIGIN, null, null, null);
		waypoint.setFound("profile");
		Assertions.assertTrue(waypoint.shouldRender());
		waypoint.setFound("");
		Assertions.assertFalse(waypoint.shouldRender());
		waypoint.setMissing();
		Assertions.assertTrue(waypoint.shouldRender());
	}

	@Test
	void testGetRenderColorComponents() {
		ProfileAwareWaypoint waypoint = new ProfileAwareWaypoint(BlockPos.ORIGIN, null, new float[]{0f, 0.5f, 1f}, new float[]{1f, 0.5f, 0f});
		waypoint.setFound("profile");
		float[] colorComponents = waypoint.getRenderColorComponents();
		Assertions.assertEquals(0f, colorComponents[0]);
		Assertions.assertEquals(0.5f, colorComponents[1]);
		Assertions.assertEquals(1f, colorComponents[2]);
		waypoint.setFound("");
		colorComponents = waypoint.getRenderColorComponents();
		Assertions.assertEquals(1f, colorComponents[0]);
		Assertions.assertEquals(0.5f, colorComponents[1]);
		Assertions.assertEquals(0f, colorComponents[2]);
		waypoint.setMissing();
		colorComponents = waypoint.getRenderColorComponents();
		Assertions.assertEquals(0f, colorComponents[0]);
		Assertions.assertEquals(0.5f, colorComponents[1]);
		Assertions.assertEquals(1f, colorComponents[2]);
	}
}
