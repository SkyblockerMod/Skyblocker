package de.hysky.skyblocker.skyblock.dwarven;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class fossilSolverTest {

	fossilSolver.container dummyContainer(){
		fossilSolver.tileState[][] tileState = new fossilSolver.tileState[6][9];
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 9; y++) {
				tileState[x][y] = fossilSolver.tileState.UNKNOWN;
			}
		}
		return new fossilSolver.container(tileState);
	}
	@Test
	void testGetPossibleStates() {
		//check there is the right amount
		Assertions.assertEquals(fossilSolver.getAllPossibleStates().size(),458);
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
		fossilSolver.container container = dummyContainer();
		container.updateSlot(0,2, fossilSolver.tileState.FOSSIL);
		container.updateSlot(0,3, fossilSolver.tileState.FOSSIL);
		container.updateSlot(0,4, fossilSolver.tileState.FOSSIL);
		container.updateSlot(0,5, fossilSolver.tileState.FOSSIL);
		container.updateSlot(1,5, fossilSolver.tileState.FOSSIL);
		Assertions.assertEquals(fossilSolver.getFossilChance(container,null)[20], 1);
	}
}
