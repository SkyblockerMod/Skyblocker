package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import java.util.Arrays;

/**
 * Represents the game board for the Boulder puzzle, managing the grid of BoulderObjects.
 * This class handles operations such as placing objects on the board, retrieving objects,
 * and generating a character representation of the game board.
 */
public class BoulderBoard {
	private final int rows;
	private final int cols;
	private final char[][] grid;

	/**
	 * Constructs a BoulderBoard with the specified height, width, and target BoulderObject.
	 *
	 * @param rows The width of the board.
	 * @param cols The height of the board.
	 */
	public BoulderBoard(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.grid = new char[rows][cols];

		for (int row = 0; row < rows; row++) {
			Arrays.fill(grid[row], '.');
		}
	}

	/**
	 * Places a BoulderObject at the specified position on the board.
	 *
	 * @param x      The x-coordinate of the position.
	 * @param y      The y-coordinate of the position.
	 * @param object The BoulderObject to place on the board.
	 */
	public void placeObject(int x, int y, char object) {
		grid[x][y] = object;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	/**
	 * Generates a character array representation of the game board.
	 * Each character represents a type of BoulderObject or an empty space.
	 *
	 * @return A 2D character array representing the game board.
	 */
	public char[][] getBoardCharArray() {
		return grid;
	}

	/**
	 * Prints the current state of the game board to the console.
	 * Each character represents a type of BoulderObject or an empty space.
	 */
	public String boardToString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				sb.append(grid[x][y]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
