package de.hysky.skyblocker.skyblock.dwarven.fossil;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FossilCalculations {
	protected static final List<Structures.permutation> POSSIBLE_STATES = getAllPossibleStates();
	private static final int EXCAVATOR_WIDTH = 9;
	private static final int EXCAVATOR_HEIGHT = 6;


	/**
	 * The number of still possible fossil permutations from last time {@link FossilCalculations#getFossilChance} was invoked
	 */
	protected static int permutations = -1;
	/**
	 * The least amount of clicks needed to uncover a fossil from last time {@link FossilCalculations#getFossilChance} was invoked
	 */
	protected static int minimumTiles;

	protected static String fossilName;

	/**
	 * Returns an array of how likely a slot is to contain a fossil assuming one has to exist
	 *
	 * @param tiles the state of the excavator window
	 * @return the probability of a fossil being in a tile
	 */

	protected static double[] getFossilChance(Structures.TileGrid tiles, String percentage) {
		int[] total = new int[EXCAVATOR_WIDTH * EXCAVATOR_HEIGHT];
		minimumTiles = EXCAVATOR_WIDTH * EXCAVATOR_HEIGHT;
		AtomicInteger fossilCount = new AtomicInteger();
		Arrays.stream(tiles.state()).forEach(row -> Arrays.stream(row).forEach(tile -> {if (tile.equals(Structures.TileState.FOSSIL)) fossilCount.getAndIncrement(); }));

		//loop though tile options and if they are valid
		List<Structures.permutation> validStates = new ArrayList<>();
		for (Structures.permutation state : POSSIBLE_STATES) {
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

		//update fossil name value
		if (validStates.isEmpty()) {
			fossilName = null;
		} else {
			//assume there is only one type of fossil and set name to value of first fossil
			fossilName = validStates.getFirst().type().name;
			for (Structures.permutation fossil : validStates) {
				//if there is more than one type of fossil reset name to null
				if (!fossil.type().name.equals(fossilName)) {
					fossilName = null;
					break;
				}
			}
		}

		//from all the valid states work out the chance of each tile being a fossil
		int index = 0;
		for (int y = 0; y < EXCAVATOR_HEIGHT; y++) {
			for (int x = 0; x < EXCAVATOR_WIDTH; x++) {
				if (tiles.getSlot(x, y) == Structures.TileState.UNKNOWN) {
					for (Structures.permutation state : validStates) {
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
	 * Converts a dictionary of item stacks to{@link Structures.TileGrid}. assuming each row will be 9 tiles and there will be 6 rows
	 *
	 * @param currentState dictionary of item in container
	 * @return input container converted into 2d {@link Structures.TileState} array
	 */
	protected static Structures.TileGrid convertItemsToTiles(Int2ObjectMap<ItemStack> currentState) {
		Structures.TileGrid output = new Structures.TileGrid(new Structures.TileState[EXCAVATOR_HEIGHT][EXCAVATOR_WIDTH]);
		//go through each slot and work out its state
		int index = 0;
		for (int y = 0; y < EXCAVATOR_HEIGHT; y++) {
			for (int x = 0; x < EXCAVATOR_WIDTH; x++) {
				Item item = currentState.get(index).getItem();
				if (item == Items.WHITE_STAINED_GLASS_PANE) {
					output.updateSlot(x, y, Structures.TileState.FOSSIL);
				} else if (item == Items.BROWN_STAINED_GLASS_PANE) {
					output.updateSlot(x, y, Structures.TileState.UNKNOWN);
				} else {
					output.updateSlot(x, y, Structures.TileState.EMPTY);
				}
				index++;
			}
		}
		return output;
	}

	/**
	 * Finds all possible fossil combinations and creates a list to return
	 *
	 * @return list of all possible fossil arrangements
	 */
	protected static List<Structures.permutation> getAllPossibleStates() {
		List<Structures.permutation> output = new ArrayList<>();
		//loop though each fossil type and for each possible rotation add valid offset of add to output list

		//loop through fossils
		for (FossilTypes fossil : FossilTypes.values()) {
			//loop though rotations
			for (Structures.TransformationOptions rotation : fossil.rotations) {
				//get the rotated grid of the fossil
				Structures.TileGrid grid = transformGrid(new Structures.TileGrid(fossil.grid), rotation);
				//get possible offsets for the grid based on width and height
				int maxXOffset = EXCAVATOR_WIDTH - grid.width();
				int maxYOffset = EXCAVATOR_HEIGHT - grid.height();
				//loop though possible offsets and for each of them create a screen state and return the value
				for (int x = 0; x <= maxXOffset; x++) {
					for (int y = 0; y <= maxYOffset; y++) {
						output.add(new Structures.permutation(fossil, grid, x, y));
					}
				}
			}
		}
		return output;
	}

	/**
	 * Transforms a grid for each of the options in {@link Structures.TransformationOptions}
	 *
	 * @param grid           input grid
	 * @param transformation transformation to perform on gird
	 * @return transformed grid
	 */
	private static Structures.TileGrid transformGrid(Structures.TileGrid grid, Structures.TransformationOptions transformation) {
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
	 *
	 * @param grid input grid
	 * @return flipped grid
	 */
	protected static Structures.TileGrid flipGrid(Structures.TileGrid grid) {
		Structures.TileGrid output = new Structures.TileGrid(new Structures.TileState[grid.height()][grid.width()]);
		for (int x = 0; x < grid.width(); x++) {
			for (int y = 0; y < grid.height(); y++) {
				output.updateSlot(x, y, grid.getSlot(x, grid.height() - 1 - y));
			}
		}
		return output;
	}

	/**
	 * Applies a rotation to a grid
	 *
	 * @param grid     input grid
	 * @param rotation rotation amount in degrees
	 * @return rotated grid
	 */
	protected static Structures.TileGrid rotateGrid(Structures.TileGrid grid, int rotation) {
		int startingWidth = grid.width() - 1;
		int startingHeight = grid.height() - 1;
		switch (rotation) {
			case 90 -> {
				Structures.TileGrid output = new Structures.TileGrid(new Structures.TileState[grid.width()][grid.height()]);
				for (int originalX = 0; originalX < grid.width(); originalX++) {
					for (int originalY = 0; originalY < grid.height(); originalY++) {
						output.updateSlot(startingHeight - originalY, originalX, grid.getSlot(originalX, originalY));
					}
				}
				return output;
			}
			case 180 -> {
				Structures.TileGrid output = new Structures.TileGrid(new Structures.TileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, startingHeight - y, grid.getSlot(x, y));
					}
				}
				return output;
			}
			case 270 -> {
				Structures.TileGrid output = new Structures.TileGrid(new Structures.TileState[grid.width()][grid.height()]);
				for (int originalX = 0; originalX < grid.width(); originalX++) {
					for (int originalY = 0; originalY < grid.height(); originalY++) {
						output.updateSlot(originalY, startingWidth - originalX, grid.getSlot(originalX, originalY));
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
