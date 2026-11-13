package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoulderSolverTest {

	private static char[][] filledBoard(char fill) {
		char[][] grid = new char[7][7];
		for (char[] row : grid) {
			Arrays.fill(row, fill);
		}
		return grid;
	}

	@Test
	void findsSeveralRoutesWithTheFastestFirst() {
		List<BoulderSolver.GameState> initial = List.of(new BoulderSolver.GameState(filledBoard('.'), 6, 3, 0));
		List<List<int[]>> solutions = BoulderSolver.aStarSolve(initial);

		assertEquals(3, solutions.size());
		//the fastest route comes first, six steps straight across the open board to the back wall, plus the start cell
		assertEquals(7, solutions.getFirst().size());
		for (List<int[]> route : solutions) {
			assertEquals(0, route.getLast()[0]);
		}
	}

	@Test
	void reportsLockedBoardsAsUnsolvable() {
		List<BoulderSolver.GameState> initial = List.of(new BoulderSolver.GameState(filledBoard('B'), 6, 3, 0));
		assertTrue(BoulderSolver.aStarSolve(initial).isEmpty());
	}
}
