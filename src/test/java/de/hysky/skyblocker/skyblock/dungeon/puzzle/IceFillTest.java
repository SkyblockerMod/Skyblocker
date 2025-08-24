package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    @Test
    void testIceFillSolve() {
        List<Vector2ic> iceFillPath = IceFill.INSTANCE.solve(iceFillBoard);
        List<Vector2ic> expectedIceFillPath = List.of(new Vector2i(6, 3), new Vector2i(5, 3), new Vector2i(4, 3), new Vector2i(3, 3), new Vector2i(3, 2), new Vector2i(4, 2), new Vector2i(5, 2), new Vector2i(6, 2), new Vector2i(6, 1), new Vector2i(5, 1), new Vector2i(5, 0), new Vector2i(4, 0), new Vector2i(4, 1), new Vector2i(3, 1), new Vector2i(3, 0), new Vector2i(2, 0), new Vector2i(1, 0), new Vector2i(0, 0), new Vector2i(0, 1), new Vector2i(1, 1), new Vector2i(2, 1), new Vector2i(2, 2), new Vector2i(1, 2), new Vector2i(1, 3), new Vector2i(1, 4), new Vector2i(1, 5), new Vector2i(2, 5), new Vector2i(3, 5), new Vector2i(3, 4), new Vector2i(4, 4), new Vector2i(5, 4), new Vector2i(6, 4), new Vector2i(6, 5), new Vector2i(6, 6), new Vector2i(5, 6), new Vector2i(5, 5), new Vector2i(4, 5), new Vector2i(4, 6), new Vector2i(3, 6), new Vector2i(2, 6), new Vector2i(1, 6), new Vector2i(0, 6), new Vector2i(0, 5), new Vector2i(0, 4), new Vector2i(0, 3));
        Assertions.assertEquals(expectedIceFillPath, iceFillPath);
    }
}
