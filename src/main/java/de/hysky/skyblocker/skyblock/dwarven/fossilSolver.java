package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Vector2i;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

public class fossilSolver extends SimpleContainerSolver {
	public fossilSolver() {
		super("Fossil Excavator");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		System.out.println("update");
		//convert to container
		container mainContainer = convertItemsToTiles(slots);
		//get chance for each
		double[] probability = getFossilChance(mainContainer);
		//get the highlight amount and return
		return convertChanceToColor(probability, 0, 0, 255); //todo better colour
	}

	@Override
	public boolean isEnabled() {
		//todo is enabled toggle
		return true;
	}

	private static List<ColorHighlight> convertChanceToColor(double[] chances, int maxR, int maxG, int maxB) {
		List<ColorHighlight> outputColors = new ArrayList<>();
		//loop though all the chance values and set the color to match probability. full color means that its 100%
		OptionalDouble highProbability = Arrays.stream(chances).max();
		System.out.println("max persent:"+highProbability);
		System.out.println(Arrays.toString(chances));
		for (int i = 0; i < chances.length; i ++) {
			double chance = chances[i];
			if (Double.isNaN(chances[i]) || chances[i] == 0) {
				continue;
			}
			if (chances[i] == highProbability.getAsDouble()){
				outputColors.add(ColorHighlight.green(i));
				continue;
			}
			outputColors.add(new ColorHighlight(i, 128 << 24 | (int)(maxR * chance) << 16| (int)(maxG * chance) << 8| (int)(maxB * chance)));
		}
		return  outputColors;
	}

	protected enum tileState {
		UNKNOWN,
		EMPTY,
		FOSSIL
	}
	protected record container(tileState[][] state){
		public void updateSlot(int x, int y, tileState newState) {
			state[y][x] = newState;
		}
		public tileState getSlot(int x, int y) {
			return state[y][x];
		}
		public int width() {
			return state[0].length;
		}
		public int height() {
			return state.length;
		}
	}

	protected enum transformationOptions {
		ROTATED_0,
		ROTATED_90,
		ROTATED_180,
		ROTATED_270,
		FLIP_ROTATED_0,
		FLIP_ROTATED_90,
		FLIP_ROTATED_180,
		FLIP_ROTATED_270;
	}

	protected enum fossilTypes { //todo add percentages so shape can be guessed
		CLAW(new tileState[][]{
				{tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY},
				{tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY}
		}, List.of(transformationOptions.values())),
		TUSK(new tileState[][]{
				{tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL},
				{tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL},
				{tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL},
				{tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY},
				{tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY}
		}, List.of(transformationOptions.values())),
		UGLY(new tileState[][]{
				{tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY},
				{tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY},
				{tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL},
				{tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY}
		},  List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_90, transformationOptions.ROTATED_180, transformationOptions.ROTATED_270)),
		HELIX(new tileState[][]{
				{tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL}, // helix
				{tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL},
				{tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL},
				{tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL}
		},  List.of(transformationOptions.values())),
		WEBBED(new tileState[][]{
				{tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY}, // webbed fossil
				{tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL},
				{tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY},
				{tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY}
		},  List.of(transformationOptions.ROTATED_0, transformationOptions.FLIP_ROTATED_0)),
		FOOTPRINT(new tileState[][]{
				{tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL}, // footprint fossil
				{tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.FOSSIL},
				{tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY},
				{tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY},
				{tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY}
		},  List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_90, transformationOptions.ROTATED_180, transformationOptions.ROTATED_270)),
		CLUBBED(new tileState[][]{
				{tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL}, // clubbed fossil
				{tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL},
				{tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY},
				{tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY,tileState.EMPTY}
		},  List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_180, transformationOptions.FLIP_ROTATED_0, transformationOptions.FLIP_ROTATED_180)),
		SPINE(new tileState[][]{
				{tileState.EMPTY,tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY,tileState.EMPTY}, // spine fossil
				{tileState.EMPTY,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.EMPTY},
				{tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL,tileState.FOSSIL}
		},  List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_90, transformationOptions.ROTATED_180, transformationOptions.ROTATED_270));

		final List<transformationOptions> rotations;
		final tileState[][] grid;

		fossilTypes(tileState[][] grid, List<transformationOptions> rotations) {
			this.grid = grid;
			this.rotations = rotations;
		}
	}

	protected record screenState(fossilTypes type, container grid, Vector2i offset) {
		/**
		 * works out if this is a valid state based on the current state of the escevator window
		 *
		 * @param currentState the state of the escevator window
		 * @return if this screen state can exist depending on found tiles
		 */
		public boolean isValid(container currentState) {
			for (int x = 0; x < grid.width(); x++) {
				for (int y = 0; y < grid.height(); y++) {
					tileState knownState = currentState.getSlot(x + offset.x, y + offset.y);
					//still do not know if the tiles will match or not so do not check
					if (knownState == tileState.UNKNOWN) {
						continue;
					}
					tileState predictedState = grid.getSlot(x, y);

					//if this screen state does not line up with the actual state it can not be valid
					if (predictedState != knownState) {
						return false;
					}
				}
			}
			//if no conflicts return ture
			return true;
		}

		public boolean isFossil(int positionX, int positionY) {
			int x = positionX - offset.x;
			int y = positionY - offset.y;
			//if they are not in range of the grid they are not a fossil
			if (x < 0 || x >= grid.width() || y < 0 || y >= grid.height()) {
				return false;
			}
			//return if position in grid is fossil
			return grid.getSlot(x, y) == tileState.FOSSIL;
		}
	}


	/**
	 * returnest a hash map of how likely a tile is to contain a fossil if its
	 *
	 * @param tiles the state of the escevator window
	 * @return the probibility of a fossil being in a tile
	 */

	protected static double[] getFossilChance(container tiles) {
		int[] total = new int[54];
		//convert the current state to a 2d array of tiles
		//tileState[][] tiles = convertItemsToTiles(currentState); todo somewere else

		//loop though tile options and if they are valid
		List<screenState> validStates = new ArrayList<>();
		for (screenState state : getAllPossibleStates()) { //todo cache possible states
			if (state.isValid(tiles)) {
				validStates.add(state);
			}
		}
		//from all the valid states work out the chance of each tile being a fossil
		int index = 0;
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 9; x++) {
				if (tiles.getSlot(x, y) == tileState.UNKNOWN) {
					for (screenState state : validStates) {
						if (state.isFossil(x, y)) {
							total[index] += 1;
						}
					}
				}
				index ++;
			}
		}


		return Arrays.stream(total).mapToDouble(x -> (double) x / validStates.size()).toArray();

	}

	/**
	 * converts a dictionry of item stacks to a 2d array representing if the slot is a fossil or not. assuming each row will be 9 tiles and there will be 6 rows
	 *
	 * @param currentState dictionary of item in container
	 * @return input contatainer converted into 2d {@link tileState} array
	 */
	private static container convertItemsToTiles(Int2ObjectMap<ItemStack> currentState) {
		container output =new container(new tileState[6][9]);
		//go through each slot and work out its state
		int index = 0;
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 9; x++) {
				Item item = currentState.get(index).getItem();
				if (item == Items.WHITE_STAINED_GLASS_PANE) {
					output.updateSlot(x, y,  tileState.FOSSIL);
				} else if (item == Items.BROWN_STAINED_GLASS_PANE) {
					output.updateSlot(x, y,  tileState.UNKNOWN);
				} else {
					output.updateSlot(x, y,  tileState.EMPTY);
				}
				index ++;
			}
		}
		return output;
	}

	/**
	 * finds all possible fossil conbinations and creates a list to return
	 *
	 * @return list of all possible fossil arrangments
	 */
	protected static List<screenState> getAllPossibleStates() { //todo probaly could be arry as the amount should be known
		List<screenState> output = new ArrayList<>();
		//loopp though each fossil type and for each posible rotation add valid ofset of add to output list

		//loop through fossils
		for (fossilTypes fossil : fossilTypes.values()) {
			//loop though roations
			for (transformationOptions rotation : fossil.rotations) {
				//get the rotated grid of the fossil
				container grid = transformGrid(new container(fossil.grid), rotation);
				//get possible offsets for the grid based on width an height
				int maxXOffset = 9 - grid.width();
				int maxYOffset = 6 - grid.height();
				//loop though possible offsets and for each of them create a screen state and return the value
				for (int x = 0; x <= maxXOffset; x++) {
					for (int y = 0; y <= maxYOffset; y++) {
						output.add(new screenState(fossil, grid, new Vector2i(x, y)));
					}
				}
			}
		}
		return output;
	}

	private static container transformGrid(container grid, transformationOptions transformation) { // todo in enum?
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

	private static container flipGrid(container grid) {
		container output = new container(new tileState[grid.height()][grid.width()]);
		for (int x = 0; x < grid.width(); x++) {
			for (int y = 0; y < grid.height(); y++) {
				output.updateSlot(x, y, grid.getSlot(x, grid.height() - 1 - y)) ;
			}
		}
		return output;
	}

	private static container rotateGrid(container grid, int roation) { // todo have i flipped x and y and comment on what its doing
		int startingWidth = grid.width() -1;
		int startingHeight = grid.height() -1;
		switch (roation) {
			case 90 -> {
				container output = new container(new tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, y, grid.getSlot(x,y)) ;
					}
				}
				return output;
			}
			case 180 -> {
				container output = new container(new tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, startingHeight - y, grid.getSlot(x, y) ) ;
					}
				}
				return output;
			}
			case 270 -> {
				container output = new container(new tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(x, startingHeight - y, grid.getSlot(x, y)) ;
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



