package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoulderSolverTest {
	@Test
	void anyPositionOnTheLastRowSolvesThePuzzle() {
		char[][] board = {
				{'B', 'T', 'B'},
				{'.', '.', '.'}
		};

		assertTrue(new BoulderSolver.GameState(board, 0, 0).isSolved());
		assertTrue(new BoulderSolver.GameState(board, 0, 1).isSolved());
		assertTrue(new BoulderSolver.GameState(board, 0, 2).isSolved());
	}

	@Test
	void rowsBeforeTheLastRowAreNotSolved() {
		char[][] board = {
				{'B', 'T', 'B'},
				{'.', '.', '.'}
		};

		assertFalse(new BoulderSolver.GameState(board, 1, 0).isSolved());
	}
}
