package de.hysky.skyblocker.skyblock.dwarven;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class MetalDetectorTest {
	@Test
	void testFindPossibleBlocks() {
		//test starting without knowing middle
		MetalDetector.updatePossibleBlocks(10.0, new Vec3(0, 0, 0));
		Assertions.assertEquals(MetalDetector.possibleBlocks.size(), 40);

		MetalDetector.updatePossibleBlocks(11.2, new Vec3(5, 0, 0));
		Assertions.assertEquals(MetalDetector.possibleBlocks.size(), 2);

		MetalDetector.updatePossibleBlocks(10.0, new Vec3(10, 0, 10));
		Assertions.assertEquals(MetalDetector.possibleBlocks.getFirst(), new Vec3i(0, 0, 10));

		//test while knowing the middle location
		MetalDetector.possibleBlocks = new ArrayList<>();
		MetalDetector.newTreasure = true;
		MetalDetector.minesCenter = new Vec3i(0, 0, 0);

		MetalDetector.updatePossibleBlocks(24.9, new Vec3(10, 1, 10));
		Assertions.assertEquals(MetalDetector.possibleBlocks.size(), 1);
		Assertions.assertEquals(MetalDetector.possibleBlocks.getFirst(), new Vec3i(1, -20, 20));
	}
}
