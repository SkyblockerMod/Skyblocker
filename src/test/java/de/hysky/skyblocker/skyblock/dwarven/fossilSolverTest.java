package de.hysky.skyblocker.skyblock.dwarven;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class fossilSolverTest {


	@Test
	void testGetPossibleStates() {
		//check there is the right amount
		Assertions.assertEquals(fossilSolver.getAllPossibleStates().size(),152);
	}

	@Test
	void testFossilChances() {
		//generate dummy value
		fossilSolver.tileState[][] tileState = new fossilSolver.tileState[6][9];
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 9; y++) {
				tileState[x][y] = fossilSolver.tileState.UNKNOWN;
			}
		}

		//check if count is correct for certain tiles
		Assertions.assertEquals(fossilSolver.getFossilChance(new fossilSolver.container(tileState))[22], 0.54);
	}
}
