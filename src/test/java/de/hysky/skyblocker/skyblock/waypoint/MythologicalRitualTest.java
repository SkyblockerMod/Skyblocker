package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MythologicalRitualTest {
	@Test
	void testFillLine() {
		Vec3[] line = new Vec3[21];
		Vec3 start = new Vec3(0, 0, 0);
		Vec3 direction = new Vec3(1, 0, 0);
		MythologicalRitual.fillLine(line, start, direction);
		for (int i = 0; i < line.length; i++) {
			Assertions.assertEquals(new Vec3(i - 10, 0, 0), line[i]);
		}
	}
}
