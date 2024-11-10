package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import de.hysky.skyblocker.skyblock.dwarven.fossilSolver.Structures.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.hysky.skyblocker.skyblock.dwarven.fossilSolver.FossilCalculations.getFossilChance;

public class FossilSolverTest {

	tileGrid dummyContainer(){
		tileState[][] tileStates = new tileState[6][9];
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 9; y++) {
				tileStates[x][y] = tileState.UNKNOWN;
			}
		}
		return new tileGrid(tileStates);
	}

	@Test
	void testPartlyFoundFossil(){
		tileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(0,2, tileState.FOSSIL);
		tileGrid.updateSlot(0,3, tileState.FOSSIL);
		tileGrid.updateSlot(0,4, tileState.FOSSIL);
		tileGrid.updateSlot(0,5, tileState.FOSSIL);
		tileGrid.updateSlot(1,5, tileState.FOSSIL);
		Assertions.assertEquals(getFossilChance(tileGrid,null)[20], 1);
	}

	@Test
	void testPartlyFoundFossilWithPercentage(){
		tileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(1,0, tileState.FOSSIL);
		tileGrid.updateSlot(3,0, tileState.FOSSIL);
		tileGrid.updateSlot(0,1, tileState.FOSSIL);
		Assertions.assertEquals(getFossilChance(tileGrid,"7.7")[29], 1);
	}

	@Test
	void testPartlyNoFossilFound(){
		tileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(6,1, tileState.EMPTY);
		tileGrid.updateSlot(1,2, tileState.EMPTY);
		tileGrid.updateSlot(3,2, tileState.EMPTY);
		tileGrid.updateSlot(4,2, tileState.EMPTY);
		tileGrid.updateSlot(7,2, tileState.EMPTY);
		tileGrid.updateSlot(1,3, tileState.EMPTY);
		tileGrid.updateSlot(2,3, tileState.EMPTY);
		tileGrid.updateSlot(5,3, tileState.EMPTY);
		tileGrid.updateSlot(5,4, tileState.EMPTY);
		Assertions.assertTrue(Double.isNaN(getFossilChance(tileGrid,null)[0]));
	}

	@Test
	void testInvalid(){
		tileGrid tileGrid = dummyContainer();
		tileGrid.updateSlot(0,0, tileState.FOSSIL);
		tileGrid.updateSlot(8,0, tileState.FOSSIL);
		Assertions.assertTrue(Double.isNaN(getFossilChance(tileGrid,null)[1]));
	}
}
