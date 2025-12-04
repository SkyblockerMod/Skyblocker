package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Represents the game board for the Boulder puzzle, managing the grid of BoulderObjects.
 * This class handles operations such as placing objects on the board, retrieving objects,
 * and generating a character representation of the game board.
 */
public class BoulderBoard {
	private final int height;
	private final int width;
	private final BoulderObject[][] grid;

	/**
	 * Constructs a BoulderBoard with the specified height, width, and target BoulderObject.
	 *
	 * @param height The height of the board.
	 * @param width  The width of the board.
	 * @param target The target BoulderObject that needs to be reached to solve the puzzle.
	 */
	public BoulderBoard(int height, int width, BoulderObject target) {
		this.height = height;
		this.width = width;
		this.grid = new BoulderObject[height][width];

		int offsetX = target.x() - 23;
		int y = 65;

		for (int z = 0; z < width; z++) {
			if (z == width / 2) {
				grid[0][z] = target;
			} else {
				grid[0][z] = new BoulderObject(offsetX, y, z, "B");
			}
			grid[height - 1][z] = new BoulderObject(24 - (3 * z), y, 6, "P");
		}
	}

	/**
	 * Retrieves the BoulderObject at the specified position on the board.
	 *
	 * @param x The x-coordinate of the position.
	 * @param y The y-coordinate of the position.
	 * @return The BoulderObject at the specified position, or null if no object is present.
	 */
	public BoulderObject getObjectAtPosition(int x, int y) {
		return isValidPosition(x, y) ? grid[x][y] : null;
	}

	/**
	 * Retrieves the 3D position of the BoulderObject at the specified position on the board.
	 *
	 * @param x The x-coordinate of the position.
	 * @param y The y-coordinate of the position.
	 * @return The BlockPos representing the 3D position of the BoulderObject,
	 * or null if no object is present at the specified position.
	 */
	public BlockPos getObject3DPosition(int x, int y) {
		BoulderObject object = getObjectAtPosition(x, y);
		return (object != null) ? object.get3DPosition().offset(Direction.Axis.Y, -1) : null;
	}

	/**
	 * Places a BoulderObject at the specified position on the board.
	 *
	 * @param x      The x-coordinate of the position.
	 * @param y      The y-coordinate of the position.
	 * @param object The BoulderObject to place on the board.
	 */
	public void placeObject(int x, int y, BoulderObject object) {
		grid[x][y] = object;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	/**
	 * Checks whether the specified position is valid within the bounds of the game board.
	 *
	 * @param x The x-coordinate of the position to check.
	 * @param y The y-coordinate of the position to check.
	 * @return {@code true} if the position is valid within the bounds of the board, {@code false} otherwise.
	 */
	private boolean isValidPosition(int x, int y) {
		return x >= 0 && y >= 0 && x < height && y < width;
	}

	/**
	 * Generates a character array representation of the game board.
	 * Each character represents a type of BoulderObject or an empty space.
	 *
	 * @return A 2D character array representing the game board.
	 */
	public char[][] getBoardCharArray() {
		char[][] boardCharArray = new char[height][width];
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				BoulderObject boulderObject = grid[x][y];
				boardCharArray[x][y] = (boulderObject != null) ? boulderObject.type().charAt(0) : '.';
			}
		}
		return boardCharArray;
	}

	/**
	 * Prints the current state of the game board to the console.
	 * Each character represents a type of BoulderObject or an empty space.
	 */
	public String boardToString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				BoulderObject boulderObject = grid[x][y];
				String displayChar = (boulderObject != null) ? boulderObject.type() : ".";
				sb.append(displayChar);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
