package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.Constants;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CrystalsLocationManagerTest {
	boolean matches(String text) {
		return CrystalsLocationsManager.TEXT_CWORDS_PATTERN.matcher(text).find();
	}

	@Test
	void testRegex() {
		Assertions.assertTrue(matches("Player: x123 y12 z123"));
		Assertions.assertTrue(matches("Player: x123, y12, z123"));
		Assertions.assertTrue(matches("Player: 123 12 123"));
		Assertions.assertTrue(matches("Player: 123 123 123"));
		Assertions.assertTrue(matches("Player: 123, 12, 123"));
		Assertions.assertTrue(matches("Player: 123, 123, 123"));
		Assertions.assertTrue(matches("Player: 123,12,123"));
		Assertions.assertTrue(matches("Player: 123,123,123"));

		Assertions.assertFalse(matches("Player: 123 1234 123"));
		Assertions.assertFalse(matches("Player: 1234 12 123"));
		Assertions.assertFalse(matches("Player: 123 12 1234"));
		Assertions.assertFalse(matches("Player: 12 12 123"));
		Assertions.assertFalse(matches("Player: 123 1 123"));
		Assertions.assertFalse(matches("Player: 123 12 12"));
		Assertions.assertFalse(matches("Player: 12312123"));
		Assertions.assertFalse(matches("Player: 123123123"));
	}

	@Test
	void testLocationInCrystals() {
		Assertions.assertTrue(CrystalsLocationsManager.checkInCrystals(new BlockPos(512, 70, 512)));

		Assertions.assertTrue(CrystalsLocationsManager.checkInCrystals(new BlockPos(202, 31, 202)));
		Assertions.assertTrue(CrystalsLocationsManager.checkInCrystals(new BlockPos(823, 188, 823)));

		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(new BlockPos(201, 31, 202)));
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(new BlockPos(202, 30, 202)));
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(new BlockPos(202, 31, 201)));

		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(new BlockPos(824, 188, 823)));
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(new BlockPos(823, 189, 823)));
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(new BlockPos(823, 188, 824)));
	}

	@Test
	void testSetLocationMessage() {
		Assertions.assertEquals(Constants.PREFIX.get().getString() + "Added waypoint for 'Jungle Temple' at 10 11 12.", CrystalsLocationsManager.getSetLocationMessage("Jungle Temple", new BlockPos(10, 11, 12)).getString());
		Assertions.assertEquals(Constants.PREFIX.get().getString() + "Added waypoint for 'Fairy Grotto' at 0 0 0.", CrystalsLocationsManager.getSetLocationMessage("Fairy Grotto", new BlockPos(0, 0, 0)).getString());
	}
}
