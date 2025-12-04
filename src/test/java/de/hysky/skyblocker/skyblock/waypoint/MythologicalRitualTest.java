package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MythologicalRitualTest {
	@Test
	void testFillLine() {
		Vec3d[] line = new Vec3d[21];
		Vec3d start = new Vec3d(0, 0, 0);
		Vec3d direction = new Vec3d(1, 0, 0);
		MythologicalRitual.fillLine(line, start, direction);
		for (int i = 0; i < line.length; i++) {
			Assertions.assertEquals(new Vec3d(i - 10, 0, 0), line[i]);
		}
	}
}
