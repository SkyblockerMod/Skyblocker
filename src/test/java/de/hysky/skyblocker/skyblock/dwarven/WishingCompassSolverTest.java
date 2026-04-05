package de.hysky.skyblocker.skyblock.dwarven;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

public class WishingCompassSolverTest {

	@Test
	void test2dSolve() {
		Vec3 startPosOne = new Vec3(100, 1, 0);
		Vec3 startPosTwo = new Vec3(0, 1, 100);
		Vec3 directionOne = new Vec3(-1, 0, 0);
		Vec3 directionTwo = new Vec3(0, 0, -1);
		Assertions.assertEquals(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo), new Vec3(0, 1, 0));

		startPosOne = new Vec3(100, 1, 100);
		startPosTwo = new Vec3(50, 1, 100);
		directionOne = new Vec3(-1, 0, -1);
		directionTwo = new Vec3(-0.5, 0, -1);
		Assertions.assertEquals(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo), new Vec3(0, 1, 0));
	}

	@Test
	void test3dSolve() {
		Vec3 startPosOne = new Vec3(100, 200, 0);
		Vec3 startPosTwo = new Vec3(0, 0, 100);
		Vec3 directionOne = new Vec3(-1, -1, 0);
		Vec3 directionTwo = new Vec3(0, 1, -1);
		Assertions.assertTrue(Objects.requireNonNull(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo)).distanceTo(new Vec3(0, 100, 0)) < 0.1);
	}

	@Test
	void testParallelSolve() {
		Vec3 startPosOne = new Vec3(100, 0, 0);
		Vec3 startPosTwo = new Vec3(50, 0, 0);
		Vec3 directionOne = new Vec3(-1, 0, 0);
		Vec3 directionTwo = new Vec3(-1, 0, 0);
		Assertions.assertNull(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo));
	}
}
