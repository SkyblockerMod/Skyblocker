package de.hysky.skyblocker.skyblock.dwarven;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class WishingCompassSolverTest {

    @Test
    void test2dSolve() {
        Vec3d startPosOne = new Vec3d(100, 1, 0);
        Vec3d startPosTwo = new Vec3d(0, 1, 100);
        Vec3d directionOne = new Vec3d(-1, 0, 0);
        Vec3d directionTwo = new Vec3d(0, 0, -1);
        Assertions.assertEquals(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo), new Vec3d(0, 1, 0));

        startPosOne = new Vec3d(100, 1, 100);
        startPosTwo = new Vec3d(50, 1, 100);
        directionOne = new Vec3d(-1, 0, -1);
        directionTwo = new Vec3d(-0.5, 0, -1);
        Assertions.assertEquals(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo), new Vec3d(0, 1, 0));
    }

    @Test
    void test3dSolve() {
        Vec3d startPosOne = new Vec3d(100, 200, 0);
        Vec3d startPosTwo = new Vec3d(0, 0, 100);
        Vec3d directionOne = new Vec3d(-1, -1, 0);
        Vec3d directionTwo = new Vec3d(0, 1, -1);
        Assertions.assertTrue(Objects.requireNonNull(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo)).distanceTo(new Vec3d(0, 100, 0))<0.1);
    }

    @Test
    void testParallelSolve() {
        Vec3d startPosOne = new Vec3d(100, 0, 0);
        Vec3d startPosTwo = new Vec3d(50, 0, 0);
        Vec3d directionOne = new Vec3d(-1, 0, 0);
        Vec3d directionTwo = new Vec3d(-1, 0, 0);
        Assertions.assertNull(WishingCompassSolver.solve(startPosOne, startPosTwo, directionOne, directionTwo));
    }
}
