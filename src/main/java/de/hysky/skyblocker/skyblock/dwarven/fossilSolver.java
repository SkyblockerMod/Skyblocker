package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class fossilSolver extends SimpleContainerSolver implements TooltipAdder {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final List<screenState> POSSIBLE_STATES = getAllPossibleStates();
	private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("Fossil Excavation Progress: (\\d{2}.\\d)%");


	private String percentage = null;
	private static int permutations = -1;
	private static int minimumTiles;
	private static double[] probability;

	public fossilSolver() {
		super("Fossil Excavator");
		percentage = null;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		//convert to container
		container mainContainer = convertItemsToTiles(slots);
		//get the fossil chance percentage
		if (percentage == null) {
			percentage = getFossilPercentage(slots);
		}
		//get chance for each
		probability = getFossilChance(mainContainer, percentage);
		//get the highlight amount and return
		return convertChanceToColor(probability, 0, 0, 255); //todo better colour
	}

	/**
	 * See if there is any found fossils then see if there is a fossil chance percentage in the tool tips
	 *
	 * @param slots items to check tool tip of
	 * @return null if there is none or the value of the percentage
	 */
	private String getFossilPercentage(Int2ObjectMap<ItemStack> slots) {
		for (ItemStack item : slots.values()) {
			for (Text line : item.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, TooltipType.BASIC)) {
				Matcher matcher = PERCENTAGE_PATTERN.matcher(line.getString());
				if (matcher.matches()) {
					return matcher.group(2);
				}
			}
		}
		return null;
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
		for (int i = 0; i < chances.length; i++) {
			double chance = chances[i];
			if (Double.isNaN(chances[i]) || chances[i] == 0) {
				continue;
			}
			if (chances[i] == highProbability.getAsDouble()) {
				outputColors.add(ColorHighlight.green(i));
				continue;
			}
			outputColors.add(new ColorHighlight(i, 128 << 24 | (int) (maxR * chance) << 16 | (int) (maxG * chance) << 8 | (int) (maxB * chance)));
		}
		return outputColors;
	}

	/**
	 * add solver info to tooltips
	 *
	 * @param focusedSlot the slot focused by the player
	 * @param stack       unused
	 * @param lines       the lines for the tooltip
	 */
	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		//add spacer
		lines.add (LineSmoothener.createSmoothLine());

		//if no permutation say this instead of other stats
		if (permutations == 0) {
			lines.add(Text.literal("No fossil found").formatted(Formatting.GOLD));
			return;
		}

		//add permutation count
		lines.add(Text.literal("Possible Patterns: ").append(Text.literal(String.valueOf(permutations)).formatted(Formatting.YELLOW)));
		//add minimum tiles left count
		lines.add(Text.literal("Minimum fossil left : ").append(Text.literal(String.valueOf(minimumTiles)).formatted(Formatting.YELLOW)));
		//add probability if available and not uncovered
		if (focusedSlot != null && probability != null && probability.length > focusedSlot.getIndex() && stack.getItem() == Items.BROWN_STAINED_GLASS_PANE) {
			lines.add(Text.literal("Probability: ").append(Text.literal(Math.round(probability[focusedSlot.getIndex()] * 100) + "%").formatted(Formatting.YELLOW)));
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

	protected enum tileState {
		UNKNOWN,
		EMPTY,
		FOSSIL
	}

	protected record container(tileState[][] state) {
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

	protected enum fossilTypes {
		CLAW(new tileState[][]{
				{tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY},
				{tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY}
		}, List.of(transformationOptions.values()), "7.7", 14),
		TUSK(new tileState[][]{
				{tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL},
				{tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY},
				{tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY}
		}, List.of(transformationOptions.values()), "12.5", 8),
		UGLY(new tileState[][]{
				{tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY},
				{tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY}
		}, List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_90, transformationOptions.ROTATED_180, transformationOptions.ROTATED_270), "6.2", 16),
		HELIX(new tileState[][]{
				{tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL}, // helix
				{tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL},
				{tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL},
				{tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL}
		}, List.of(transformationOptions.values()), "7.1", 14),
		WEBBED(new tileState[][]{
				{tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY}, // webbed fossil
				{tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY},
				{tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY}
		}, List.of(transformationOptions.ROTATED_0, transformationOptions.FLIP_ROTATED_0), "10", 10),
		FOOTPRINT(new tileState[][]{
				{tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL}, // footprint fossil
				{tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.FOSSIL},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY},
				{tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY}
		}, List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_90, transformationOptions.ROTATED_180, transformationOptions.ROTATED_270), "7.7", 13),
		CLUBBED(new tileState[][]{
				{tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL}, // clubbed fossil
				{tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL},
				{tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY},
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY, tileState.EMPTY}
		}, List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_180, transformationOptions.FLIP_ROTATED_0, transformationOptions.FLIP_ROTATED_180), "9.1", 11),
		SPINE(new tileState[][]{
				{tileState.EMPTY, tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY, tileState.EMPTY}, // spine fossil
				{tileState.EMPTY, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.EMPTY},
				{tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL, tileState.FOSSIL}
		}, List.of(transformationOptions.ROTATED_0, transformationOptions.ROTATED_90, transformationOptions.ROTATED_180, transformationOptions.ROTATED_270), "8.3", 12);

		final List<transformationOptions> rotations;
		final tileState[][] grid;
		final String percentage;
		final int tileCount;

		fossilTypes(tileState[][] grid, List<transformationOptions> rotations, String percentage, int tileCount) {
			this.grid = grid;
			this.rotations = rotations;
			this.percentage = percentage;
			this.tileCount = tileCount; //todo just have tile count
		}
	}

	protected record screenState(fossilTypes type, container grid, int xOffset, int yOffset) {
		/**
		 * works out if this is a valid state based on the current state of the excavator window
		 *
		 * @param currentState the state of the excavator window
		 * @return if this screen state can exist depending on found tiles
		 */
		public boolean isValid(container currentState, String percentage) {
			//check the percentage
			if (percentage != null && !percentage.equals(type.percentage)) {
				return false;
			}
			//check conflicting tiles
			for (int x = 0; x < currentState.width(); x++) {
				for (int y = 0; y < currentState.height(); y++) {
					tileState knownState = currentState.getSlot(x, y);
					//if there is a miss match return false
					switch (knownState) {
						case UNKNOWN -> {
							//still do not know if the tiles will match or not so carry on
							continue;
						}
						case FOSSIL -> {
							if (!isFossilCollision(x, y)) {
								return false;
							}
						}
						case EMPTY -> {
							if (!isEmptyCollision(x, y)) {
								return false;
							}
						}
					}
				}
			}
			//if no conflicts return ture
			return true;
		}

		public boolean isEmptyCollision(int positionX, int positionY) {
			try {
				return isState(positionX, positionY, tileState.EMPTY);
			} catch (IndexOutOfBoundsException f) {
				return true;
			}
		}

		public boolean isFossilCollision(int positionX, int positionY) {
			try {
				return isState(positionX, positionY, tileState.FOSSIL);
			} catch (IndexOutOfBoundsException f) {
				return false;
			}
		}

		private boolean isState(int positionX, int positionY, tileState state) {
			int x = positionX - xOffset;
			int y = positionY - yOffset;
			//if they are not in range of the grid they are not a fossil
			if (x < 0 || x >= grid.width() || y < 0 || y >= grid.height()) {
				throw new IndexOutOfBoundsException("not in grid");
			}
			//return if position in grid is fossil
			return grid.getSlot(x, y) == state;
		}
	}


	/**
	 * returns a hash map of how likely a tile is to contain a fossil if its
	 *
	 * @param tiles the state of the excavator window
	 * @return the probability of a fossil being in a tile
	 */

	protected static double[] getFossilChance(container tiles, String percentage) {
		int[] total = new int[54];
		minimumTiles = 100;
		AtomicInteger fossilCount = new AtomicInteger();
		Arrays.stream(tiles.state()).forEach(row -> Arrays.stream(row).forEach(tile -> {if (tile.equals(tileState.FOSSIL)) fossilCount.getAndIncrement();}));

		//loop though tile options and if they are valid
		List<screenState> validStates = new ArrayList<>();
		for (screenState state : POSSIBLE_STATES) {
			if (state.isValid(tiles, percentage)) {
				validStates.add(state);
				//update minimum left if it's smaller than current value
				int min = state.type.tileCount - fossilCount.get();
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
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 9; x++) {
				if (tiles.getSlot(x, y) == tileState.UNKNOWN) {
					for (screenState state : validStates) {
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
	 * converts a dictionary of item stacks to a 2d array representing if the slot is a fossil or not. assuming each row will be 9 tiles and there will be 6 rows
	 *
	 * @param currentState dictionary of item in container
	 * @return input container converted into 2d {@link tileState} array
	 */
	private static container convertItemsToTiles(Int2ObjectMap<ItemStack> currentState) {
		container output = new container(new tileState[6][9]);
		//go through each slot and work out its state
		int index = 0;
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 9; x++) {
				Item item = currentState.get(index).getItem();
				if (item == Items.WHITE_STAINED_GLASS_PANE) {
					output.updateSlot(x, y, tileState.FOSSIL);
				} else if (item == Items.BROWN_STAINED_GLASS_PANE) {
					output.updateSlot(x, y, tileState.UNKNOWN);
				} else {
					output.updateSlot(x, y, tileState.EMPTY);
				}
				index++;
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
						output.add(new screenState(fossil, grid, x, y));
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
				output.updateSlot(x, y, grid.getSlot(x, grid.height() - 1 - y));
			}
		}
		return output;
	}

	private static container rotateGrid(container grid, int roation) { // todo have i flipped x and y and comment on what its doing
		int startingWidth = grid.width() - 1;
		int startingHeight = grid.height() - 1;
		switch (roation) {
			case 90 -> {
				container output = new container(new tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, y, grid.getSlot(x, y));
					}
				}
				return output;
			}
			case 180 -> {
				container output = new container(new tileState[grid.height()][grid.width()]);
				for (int x = 0; x < grid.width(); x++) {
					for (int y = 0; y < grid.height(); y++) {
						output.updateSlot(startingWidth - x, startingHeight - y, grid.getSlot(x, y));
					}
				}
				return output;
			}
			case 270 -> {
				container output = new container(new tileState[grid.height()][grid.width()]);
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



