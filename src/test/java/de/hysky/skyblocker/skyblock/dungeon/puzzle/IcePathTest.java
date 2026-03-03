package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import org.joml.Vector2i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IcePathTest {
	private static final boolean[][] silverfishBoard = new boolean[][]{
			{false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false},
			{false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false, false},
			{true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false},
			{false, true, false, false, false, false, false, false, false, false, true, false, false, false, false, true, false},
			{false, false, true, false, false, false, false, false, false, false, false, true, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, false, false, false, true, false, false, false, false, false, false},
			{false, false, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false}
	};

	@Test
	void testSilverfishSolve() {
		for (int i = 0; i < silverfishBoard.length; i++) {
			System.arraycopy(silverfishBoard[i], 0, IcePath.INSTANCE.silverfishBoard[i], 0, silverfishBoard[i].length);
		}
		IcePath.INSTANCE.silverfishPos = new Vector2i(15, 15);
		IcePath.INSTANCE.solve();
		List<Vector2i> expectedSilverfishPath = List.of(new Vector2i(15, 15), new Vector2i(15, 11), new Vector2i(16, 11), new Vector2i(16, 3), new Vector2i(0, 3), new Vector2i(0, 4), new Vector2i(1, 4), new Vector2i(1, 2), new Vector2i(10, 2), new Vector2i(10, 9), new Vector2i(0, 9));
		Assertions.assertEquals(expectedSilverfishPath, IcePath.INSTANCE.silverfishPath);
	}
}
