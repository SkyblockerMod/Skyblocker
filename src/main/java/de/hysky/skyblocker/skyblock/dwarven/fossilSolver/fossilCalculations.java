package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class fossilCalculations {
	private static final List<structures.permutation> POSSIBLE_STATES = getAllPossibleStates();
	private static final int EXCAVATOR_WIDTH = 9;
	private static final int EXCAVATOR_HEIGHT = 6;


	/**
	 * the number of still possible fossil permutations from last time {@link fossilCalculations#getFossilChance} was invoked
	 */
	protected static int permutations = -1;
	/**
	 * the least amount of clicks needed to uncover a fossil from last time {@link fossilCalculations#getFossilChance} was invoked
	 */
	protected static int minimumTiles;

	/**
	 * returns an array of how likely a slot is to contain a fossil assuming one has to exist
	 *
	 * @param tiles the state of the excavator window
	 * @return the probability of a fossil being in a tile
	 */

	protected static double[] getFossilChance(structures.tileGrid tiles, String percentage) {
		int[] total = new int[EXCAVATOR_WIDTH * EXCAVATOR_HEIGHT];
		minimumTiles = EXCAVATOR_WIDTH * EXCAVATOR_HEIGHT;
		AtomicInteger fossilCount = new AtomicInteger();
		Arrays.stream(tiles.state()).forEach(row -> Arrays.stream(row).forEach(tile -> {if (tile.equals(structures.tileState.FOSSIL)) fossilCount.getAndIncrement();}));

		//loop though tile options and if they are valid
		List<structures.permutation> validStates = new ArrayList<>();
		for (structures.permutation state : POSSIBLE_STATES) {
			if (state.isValid(tiles, percentage)) {
				validStates.add(state);
				//update minimum left if it's smaller than current value
				int min = state.type().tileCount - fossilCount.get();
				if (min < minimumTiles) {
					minimumTiles = min;
				}
			}
		}
		//update permutations value
		permutations = validStates.size();
		;

		//from all the valid states work out the chance of each tile being a fossil
		int index = 0;
		for (int y = 0; y < EXCAVATOR_HEIGHT; y++) {
			for (int x = 0; x < EXCAVATOR_WIDTH; x++) {
				if (tiles.getSlot(x, y) == structures.tileState.UNKNOWN) {
					for (structures.permutation state : validStates) {
						if (state.isFossilCollision(x, y)) {
							total[index] += 1;
						}
					}
				}
				index++;
			}
		}


		return Arrays.stream(total).mapToDouble(x -> (double) x / validStates.size()).toArray();
	}

	/**
	 * converts a dictionary of item stacks to{@link structures.tileGrid}. assuming each row will be 9 tiles and there will be 6 rows
	 *
	 * @param currentState dictionary of item in container
	 * @return input container converted into 2d {@link structures.tileState} array
	 */
	protected static structures.tileGrid convertItemsToTiles(Int2ObjectMap<ItemStack> currentState) {
		structures.tileGrid output = new structures.tileGrid(new structures.tileState[EXCAVATOR_HEIGHT][EXCAVATOR_WIDTH]);
		//go through each slot and work out its state
		int index = 0;
		for (int y = 0; y < EXCAVATOR_HEIGHT; y++) {
			for (int x = 0; x < EXCAVATOR_WIDTH; x++) {
				Item item = currentState.get(index).getItem();
				if (item == Items.WHITE_STAINED_GLASS_PANE) {
					output.updateSlot(x, y, structures.tileState.FOSSIL);
				} else if (item == Items.BROWN_STAINED_GLASS_PANE) {
					output.updateSlot(x, y, structures.tileState.UNKNOWN);
				} else {
					output.updateSlot(x, y, structures.tileState.EMPTY);
				}
				index++;
			}
		}
		return output;
	}

	/**
	 * finds all possible fossil combinations and creates a list to return
	 *
	 * @return list of all possible fossil arrangements
	 */
	protected static List<structures.permutation> getAllPossibleStates() { //todo probaly could be arry as the amount should be known
		List<structures.permutation> output = new ArrayList<>();
		//loop though each fossil type and for each possible rotation add valid offset of add to output list

		//loop through fossils
		for (fossilTypes fossil : fossilTypes.values()) {
			//loop though rotations
			for (structures.transformationOptions rotation : fossil.rotations) {
				//get the rotated grid of the fossil
				structures.tileGrid grid = transformGrid(new structures.tileGrid(fossil.grid), rotation);
				//get possible offsets for the grid based on width and height
				int maxXOffset = EXCAVATOR_WIDTH - grid.width();
				int maxYOffset = EXCAVATOR_HEIGHT - grid.height();
				//loop though possible offsets and for each of them create a screen state and return the value
				for (int x = 0; x <= maxXOffset; x++) {
					for (int y = 0; y <= maxYOffset; y++) {
						output.add(new structures.permutation(fossil, grid, x, y));
					}
				}
			}
		}
		return output;
	}

	/**
	 * Transforms a grid for each of the options in {@link structures.transformationOptions}
	 * @param grid input grid
	 * @param transformation transformation to perform on gird
	 * @return transformed grid
	 */
	private static structures.tileGrid transformGrid(structures.tileGrid grid, structures.transformationOptions transformation) {
		switch (transformation) {
			case ROTATED_90 -> {
				return rotateGrid(grid, 90);
			}
			case ROTATED_180 -> {
				return rotateGrid(grid, 180);
			}
			case ROTATED_270 -> {
				return rotateGrid(grid, 270);
			}
			case FLIP_ROTATED_0 -> {
				return flipGrid(grid);
			}
			case FLIP_ROTATED_90 -> {
				return rotateGrid(flipGrid(grid), 90);
			}
			case FLIP_ROTATED_180 -> {
				return rotateGrid(flipGrid(grid), 180);
			}
			case FLIP_ROTATED_270 -> {
				return rotateGrid(flipGrid(grid), 270);
			}
			default -> {
				return grid;
			}
		}
	}

	/**
	 * Flips the grid along the vertical axis
	 * @param grid input grid
	 * @return flipped grid
	 */
	private static structures.tileGrid flipGrid(structures.tileGrid grid) {
		structures.tileGrid output = new structures.tileGrid(new structures.tileState[grid.height()][grid.width()]);
		for (int x = 0; x < grid.width(); x++) {
			for (int y = 0; y < grid.height(); y++) {
				output.updateSlot(x, y, grid.getSlot(x, grid.height() - 1 - y));
			}
		}
		return output;
	}

	/**
	 * Applies a rotation to a grid
	 * @param grid input grid
	 * @param rotation rotation amount in degrees
	 * @return rotated grid
	 */
	private static structures.tileGrid rotateGrid(structures.tileGrid grid, int rotation) {
		int startingWidth = grid.width() - 1;
		int startingHeight = grid.height() - 1;
		switch (rotation) {
			case 90 -> {
				structures.tileGrid output = new structures.tileGrid(new structures.tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, y, grid.getSlot(x, y));
					}
				}
				return output;
			}
			case 180 -> {
				structures.tileGrid output = new structures.tileGrid(new structures.tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, startingHeight - y, grid.getSlot(x, y));
					}
				}
				return output;
			}
			case 270 -> {
				structures.tileGrid output = new structures.tileGrid(new structures.tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(x, startingHeight - y, grid.getSlot(x, y));
					}
				}
				return output;
			}
			default -> {
				return grid;
			}
		}
	}
}
