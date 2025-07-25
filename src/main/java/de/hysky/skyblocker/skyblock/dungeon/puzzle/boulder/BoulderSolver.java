package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import it.unimi.dsi.fastutil.Pair;

/**
 * A utility class that provides methods to solve the Boulder puzzle using the A* search algorithm.
 * The BoulderSolver class is responsible for finding the shortest path from the starting position
 * to the target position by exploring possible moves and evaluating their costs.
 */
public class BoulderSolver {

	/**
	 * Finds the shortest path to solve the Boulder puzzle using the A* search algorithm.
	 *
	 * @param initialStates The list of initial game states from which to start the search.
	 * @return A list of coordinates representing the shortest path to solve the puzzle,
	 * or null if no solution is found within the maximum number of iterations.
	 */
	public static List<int[]> aStarSolve(List<GameState> initialStates) {
		Set<GameState> visited = new HashSet<>();
		PriorityQueue<Pair<GameState, List<int[]>>> queue = new PriorityQueue<>(new AStarComparator());

		for (GameState initialState : initialStates) {
			queue.add(Pair.of(initialState, new ArrayList<>()));
		}

		int maxIterations = 10000;
		int iterations = 0;

		while (!queue.isEmpty() && iterations < maxIterations) {
			Pair<GameState, List<int[]>> pair = queue.poll();
			GameState state = pair.left();
			List<int[]> path = pair.right();

			if (state.isSolved()) {
				return path;
			}

			if (visited.contains(state)) {
				continue;
			}
			visited.add(state);

			int[] currentCoord = {state.playerX, state.playerY};
			path.add(currentCoord);

			for (int[] direction : new int[][]{{-1, 0}, {0, -1}, {0, 1}, {1, 0}}) {
				GameState newState = new GameState(state.grid, state.playerX, state.playerY);
				if (newState.movePlayer(direction[0], direction[1])) {
					queue.add(Pair.of(newState, new ArrayList<>(path)));
				}
			}
			iterations++;
		}

		return null;
	}

	/**
	 * A comparator used to compare game states based on their A* search cost.
	 * States with lower costs are prioritized for exploration.
	 */
	private static class AStarComparator implements Comparator<Pair<GameState, List<int[]>>> {
		/**
		 * Compares two pairs of game states and their associated paths based on their costs.
		 *
		 * @param a The first pair to compare.
		 * @param b The second pair to compare.
		 * @return A negative integer if a has a lower cost than b,
		 * a positive integer if a has a higher cost than b,
		 * or zero if both have the same cost.
		 */
		@Override
		public int compare(Pair<GameState, List<int[]>> a, Pair<GameState, List<int[]>> b) {
			int costA = a.right().size() + a.left().heuristic();
			int costB = b.right().size() + b.left().heuristic();
			return Integer.compare(costA, costB);
		}
	}

	/**
	 * Represents the game state for the Boulder puzzle, including the current grid configuration
	 * and the position of the theoretical player.
	 */
	public static class GameState {
		private final char[][] grid;
		private int playerX;
		private int playerY;

		/**
		 * Constructs a new game state with the specified grid and theoretical player position.
		 *
		 * @param grid    The grid representing the Boulder puzzle configuration.
		 * @param playerX The x-coordinate of the player's position.
		 * @param playerY The y-coordinate of the player's position.
		 */
		public GameState(char[][] grid, int playerX, int playerY) {
			this.grid = copyGrid(grid);
			this.playerX = playerX;
			this.playerY = playerY;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			GameState gameState = (GameState) obj;
			return Arrays.deepEquals(grid, gameState.grid) && playerX == gameState.playerX && playerY == gameState.playerY;
		}

		@Override
		public int hashCode() {
			int result = Arrays.deepHashCode(grid);
			result = 31 * result + playerX;
			result = 31 * result + playerY;
			return result;
		}

		/**
		 * Moves the theoretical player in the specified direction and updates the game state accordingly.
		 *
		 * @param dx The change in x-coordinate (horizontal movement).
		 * @param dy The change in y-coordinate (vertical movement).
		 * @return true if the move is valid and the player is moved, false otherwise.
		 */
		public boolean movePlayer(int dx, int dy) {
			int newX = playerX + dx;
			int newY = playerY + dy;

			if (isValidPosition(newX, newY)) {
				if (grid[newX][newY] == 'B') {
					int nextToBoxX = newX + dx;
					int nextToBoxY = newY + dy;
					if (isValidPosition(nextToBoxX, nextToBoxY) && grid[nextToBoxX][nextToBoxY] == '.') {
						grid[newX][newY] = '.';
						grid[nextToBoxX][nextToBoxY] = 'B';
						playerX = newX;
						playerY = newY;
						return true;
					}
				} else {
					playerX = newX;
					playerY = newY;
					return true;
				}
			}
			return false;
		}

		private boolean isValidPosition(int x, int y) {
			return x >= 0 && y >= 0 && x < grid.length && y < grid[0].length;
		}

		/**
		 * Checks if the puzzle is solved, i.e., if the player is positioned on the target BoulderObject.
		 *
		 * @return true if the theoretical puzzle is solved, false otherwise.
		 */
		public boolean isSolved() {
			return grid[playerX][playerY] == 'T';
		}

		/**
		 * Calculates the heuristic value for the current game state, representing the estimated
		 * distance from the player's position to the target BoulderObject.
		 *
		 * @return The heuristic value for the current game state.
		 */
		public int heuristic() {
			// should be improved maybe prioritize empty path first
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					if (grid[i][j] == 'T') {
						return Math.abs(playerX - i) + Math.abs(playerY - j);
					}
				}
			}
			return Integer.MAX_VALUE;
		}

		/**
		 * Creates a deep copy of the grid array to avoid modifying the original grid.
		 *
		 * @param original The original grid array to copy.
		 * @return A deep copy of the original grid array.
		 */
		private char[][] copyGrid(char[][] original) {
			char[][] copy = new char[original.length][];
			for (int i = 0; i < original.length; i++) {
				copy[i] = original[i].clone();
			}
			return copy;
		}
	}
}
