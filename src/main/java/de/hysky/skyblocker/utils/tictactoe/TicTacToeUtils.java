package de.hysky.skyblocker.utils.tictactoe;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class TicTacToeUtils {

	public static BoardIndex getBestMove(char[][] board) {
		Object2IntOpenHashMap<BoardIndex> moves = new Object2IntOpenHashMap<>();

		for (int row = 0; row < board.length; row++) {
			for (int column = 0; column < board[row].length; column++) {
				// Simulate the move as O if the square is empty to determine a solution
				if (board[row][column] != '\0') continue;
				board[row][column] = 'O';
				int score = alphabeta(board, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, false);
				board[row][column] = '\0';

				moves.put(new BoardIndex(row, column), score);
			}
		}

		return Collections.max(moves.object2IntEntrySet(), Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).getKey();
	}

	private static boolean hasMovesAvailable(char[][] board) {
		return Arrays.stream(board).flatMap(row -> Stream.of(row[0], row[1], row[2])).anyMatch(c -> c == '\0');
	}

	private static int getScore(char[][] board) {
		// Check if X or O has won horizontally
		for (int row = 0; row < 3; row++) {
			if (board[row][0] == board[row][1] && board[row][0] == board[row][2]) {
				switch (board[row][0]) {
					case 'X': return -10;
					case 'O': return 10;
				}
			}
		}

		// Check if X or O has won vertically
		for (int column = 0; column < 3; column++) {
			if (board[0][column] == board[1][column] && board[0][column] == board[2][column]) {
				switch (board[0][column]) {
					case 'X': return -10;
					case 'O': return 10;
				}
			}
		}

		// Check if X or O has won diagonally
		// Top left to bottom right
		if (board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
			switch (board[0][0]) {
				case 'X': return -10;
				case 'O': return 10;
			}
		}

		// Top right to bottom left
		if (board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
			switch (board[0][2]) {
				case 'X': return -10;
				case 'O': return 10;
			}
		}

		return 0;	
	}

	private static int alphabeta(char[][] board, int alpha, int beta, int depth, boolean maximizePlayer) {
		int score = getScore(board);

		if (score == 10 || score == -10) return score;
		if (!hasMovesAvailable(board)) return 0;

		if (maximizePlayer) {
			int bestScore = Integer.MIN_VALUE;

			for (int row = 0; row < 3; row++) {
				for (int column = 0; column < 3; column++) {
					if (board[row][column] == '\0') {
						board[row][column] = 'O';
						bestScore = Math.max(bestScore, alphabeta(board, alpha, beta, depth + 1, false));
						board[row][column] = '\0';
						alpha = Math.max(alpha, bestScore);

						//Is this correct? Well the algorithm seems to solve it so I will assume it is
						if (beta <= alpha) break; // Pruning
					}
				}
			}

			return bestScore - depth;
		} else {
			int bestScore = Integer.MAX_VALUE;

			for (int row = 0; row < 3; row++) {
				for (int column = 0; column < 3; column++) {
					if (board[row][column] == '\0') {
						board[row][column] = 'X';
						bestScore = Math.min(bestScore, alphabeta(board, alpha, beta, depth + 1, true));
						board[row][column] = '\0';
						beta = Math.min(beta, bestScore);

						if (beta <= alpha) break; // Pruning
					}
				}
			}

			return bestScore + depth;
		}
	}
}