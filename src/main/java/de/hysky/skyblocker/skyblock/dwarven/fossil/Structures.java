package de.hysky.skyblocker.skyblock.dwarven.fossil;

import java.util.Arrays;

public class Structures {
	/**
	 * The three possible states a tile could be in
	 */
	protected enum TileState {
		UNKNOWN,
		EMPTY,
		FOSSIL
	}

	protected enum TransformationOptions {
		ROTATED_0,
		ROTATED_90,
		ROTATED_180,
		ROTATED_270,
		FLIP_ROTATED_0,
		FLIP_ROTATED_90,
		FLIP_ROTATED_180,
		FLIP_ROTATED_270;
	}

	/**
	 * Stores a grid of {@link TileState} as a 2d array referencable with an x and y
	 *
	 * @param state the starting state of the grid
	 */
	protected record TileGrid(TileState[][] state) {
		void updateSlot(int x, int y, TileState newState) {
			state[y][x] = newState;
		}

		TileState getSlot(int x, int y) {
			return state[y][x];
		}

		int width() {
			return state[0].length;
		}

		int height() {
			return state.length;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o instanceof TileGrid(TileState[][] state2)) {
				return Arrays.deepEquals(state, state2);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(state);
		}
	}

	/**
	 * Stores all the different value a permutation of a fossil can have
	 *
	 * @param type    what the type of fossil it is
	 * @param grid    what the grid will look like for the fossil
	 * @param xOffset where it's positioned in the excavator window in the x direction
	 * @param yOffset where it's positioned in the excavator window in the y direction
	 */
	protected record permutation(FossilTypes type, TileGrid grid, int xOffset, int yOffset) {
		/**
		 * Works out if this is a valid state based on the current state of the excavator window
		 *
		 * @param currentState the state of the excavator window
		 * @return if this screen state can exist depending on found tiles
		 */
		boolean isValid(TileGrid currentState, String percentage) {
			//check the percentage
			if (percentage != null && !percentage.equals(type.percentage)) {
				return false;
			}
			//check conflicting tiles
			for (int x = 0; x < currentState.width(); x++) {
				for (int y = 0; y < currentState.height(); y++) {
					TileState knownState = currentState.getSlot(x, y);
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

		/**
		 * If an empty tile is able to exist in this position in the excavator window and the permutation to still be valid
		 * (will still be valid if out of bound of the permutation)
		 *
		 * @param positionX pos x
		 * @param positionY pos y
		 * @return if the fossil is valid
		 */
		private boolean isEmptyCollision(int positionX, int positionY) {
			try {
				return isState(positionX, positionY, TileState.EMPTY);
			} catch (IndexOutOfBoundsException f) {
				return true;
			}
		}

		/**
		 * If a fossil is able to exist in this position in the excavator window and the permutation to still be valid
		 * (will not be valid of out of bounds of the permutation)
		 *
		 * @param positionX pos x
		 * @param positionY pos y
		 * @return if the fossil is valid
		 */
		boolean isFossilCollision(int positionX, int positionY) {
			try {
				return isState(positionX, positionY, TileState.FOSSIL);
			} catch (IndexOutOfBoundsException f) {
				return false;
			}
		}

		/**
		 * Returns true if the given state and this permutation line up at a given location
		 *
		 * @param positionX position in the excavator window x
		 * @param positionY position in the excavator window y
		 * @param state     the state the excavator window is at this location
		 * @return if states match
		 */
		private boolean isState(int positionX, int positionY, TileState state) {
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
}
