package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoulderSolverTest {
	private static final char[][] BOARD_1 = {
			{'B', '.', '.', '.', '.', '.', 'B'},
			{'B', 'B', 'B', '.', 'B', 'B', 'B'},
			{'B', '.', 'B', 'B', 'B', '.', 'B'},
			{'B', 'B', '.', '.', '.', 'B', 'B'},
			{'B', '.', 'B', 'B', 'B', '.', 'B'}
	};

	@Test
	void testBoulderSolver1() {
		BoulderBoard board = new BoulderBoard(7, 7);
		for (int i = 0; i < BOARD_1.length; i++) {
			for (int j = 0; j < BOARD_1[i].length; j++) {
				board.placeObject(i, j, BOARD_1[i][j]);
			}
		}
		Assertions.assertNotNull(BoulderSolver.aStarSolve(Boulder.getInitialStates(board)));
	}
}
