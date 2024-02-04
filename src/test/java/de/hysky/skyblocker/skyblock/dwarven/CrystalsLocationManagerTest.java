package de.hysky.skyblocker.skyblock.dwarven;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.Constants;

public class CrystalsLocationManagerTest {

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
        Assertions.assertEquals(CrystalsLocationsManager.getSetLocationMessage("Jungle Temple", new BlockPos(10, 11, 12)).getString(), Constants.PREFIX.get().getString() + "Added waypoint for Jungle Temple at : 10 11 12.");
        Assertions.assertEquals(CrystalsLocationsManager.getSetLocationMessage("Fairy Grotto", new BlockPos(0, 0, 0)).getString(), Constants.PREFIX.get().getString() + "Added waypoint for Fairy Grotto at : 0 0 0.");
    }
}
