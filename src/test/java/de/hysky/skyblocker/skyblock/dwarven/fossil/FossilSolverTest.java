package de.hysky.skyblocker.skyblock.dwarven.fossil;

import de.hysky.skyblocker.skyblock.dwarven.fossil.Structures.TileGrid;
import de.hysky.skyblocker.skyblock.dwarven.fossil.Structures.TileState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.hysky.skyblocker.skyblock.dwarven.fossil.FossilCalculations.getFossilChance;

public class FossilSolverTest {

	TileGrid dummyContainer() {
		TileState[][] tileStates = new TileState[6][9];
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 9; y++) {
				tileStates[x][y] = TileState.UNKNOWN;
			}
		}
		return new TileGrid(tileStates);
	}

	@Test
	void testPartlyFoundFossil() {
		TileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(0, 2, TileState.FOSSIL);
		tileGrid.updateSlot(0, 3, TileState.FOSSIL);
		tileGrid.updateSlot(0, 4, TileState.FOSSIL);
		tileGrid.updateSlot(0, 5, TileState.FOSSIL);
		tileGrid.updateSlot(1, 5, TileState.FOSSIL);
		Assertions.assertEquals(getFossilChance(tileGrid, null)[20], 1);
	}

	@Test
	void testPartlyFoundFossilWithPercentage() {
		TileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(1, 0, TileState.FOSSIL);
		tileGrid.updateSlot(3, 0, TileState.FOSSIL);
		tileGrid.updateSlot(0, 1, TileState.FOSSIL);
		Assertions.assertEquals(getFossilChance(tileGrid, "7.7")[29], 1);
	}

	@Test
	void testPartlyNoFossilFound() {
		TileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(6, 1, TileState.EMPTY);
		tileGrid.updateSlot(1, 2, TileState.EMPTY);
		tileGrid.updateSlot(3, 2, TileState.EMPTY);
		tileGrid.updateSlot(4, 2, TileState.EMPTY);
		tileGrid.updateSlot(7, 2, TileState.EMPTY);
		tileGrid.updateSlot(1, 3, TileState.EMPTY);
		tileGrid.updateSlot(2, 3, TileState.EMPTY);
		tileGrid.updateSlot(5, 3, TileState.EMPTY);
		tileGrid.updateSlot(5, 4, TileState.EMPTY);
		Assertions.assertTrue(Double.isNaN(getFossilChance(tileGrid, null)[0]));
	}

	@Test
	void testInvalid() {
		TileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(0, 0, TileState.FOSSIL);
		tileGrid.updateSlot(8, 0, TileState.FOSSIL);
		Assertions.assertTrue(Double.isNaN(getFossilChance(tileGrid, null)[1]));
	}
}
