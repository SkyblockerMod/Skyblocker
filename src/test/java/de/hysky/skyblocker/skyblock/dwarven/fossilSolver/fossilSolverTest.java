package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import de.hysky.skyblocker.skyblock.dwarven.fossilSolver.structures.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.hysky.skyblocker.skyblock.dwarven.fossilSolver.fossilCalculations.getAllPossibleStates;
import static de.hysky.skyblocker.skyblock.dwarven.fossilSolver.fossilCalculations.getFossilChance;

public class fossilSolverTest {

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
	void testGetPossibleStates() {
		//check there is the right amount
		Assertions.assertEquals(getAllPossibleStates().size(),458);
	}

	@Test
	void testFossilChances() {
		//check if count is correct start
		//Assertions.assertEquals(fossilSolver.getFossilChance(container,null)[22], 0.54);

		//check second if fossil at first
		//tileState[2][4] = fossilSolver.tileState.FOSSIL;
		//Assertions.assertEquals(fossilSolver.getFossilChance(container,null)[31], 0.65);


		//add start of helix and see if it find it


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
}
