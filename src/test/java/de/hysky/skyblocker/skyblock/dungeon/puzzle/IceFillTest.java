package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import org.joml.Vector2ic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class IceFillTest {
    private static final boolean[][] iceFillBoard = new boolean[][]{
            {false, false, true, false, false, false, false},
            {false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false},
            {false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false},
            {true, false, false, false, false, false, false},
    };
    private static final List<Vector2ic> iceFillPath = new ArrayList<>();

    @Test
    void testIceFillSolve() {
        IceFill.INSTANCE.solve(iceFillBoard, iceFillPath);
        System.out.println(iceFillPath);
        System.out.println(iceFillPath.size());
    }
}
