package de.hysky.skyblocker.skyblock.dwarven;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.Constants;

class CrystalsLocationManagerTest {
    boolean matches(String text) {
        return CrystalsLocationsManager.TEXT_CWORDS_PATTERN.matcher(text).find();
    }

    @Test
    void testRegex() {
        Assertions.assertTrue(matches("x123 y12 z123"));
        Assertions.assertTrue(matches("x123, y12, z123"));
        Assertions.assertTrue(matches("Player: 123 12 123")); //This and the ones below fail when specified in the same format as those above, as the regex check assumes that the message is sent somewhere in a message and that it isn't the whole message
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
        Assertions.assertEquals(CrystalsLocationsManager.getSetLocationMessage("Jungle Temple", new BlockPos(10, 11, 12)).getString(), Constants.PREFIX.get().getString() + "skyblocker.config.mining.crystalsWaypoints.addedWaypointJungle Temple skyblocker.config.mining.crystalsWaypoints.addedWaypoint.at : 10 11 12.");
        Assertions.assertEquals(CrystalsLocationsManager.getSetLocationMessage("Fairy Grotto", new BlockPos(0, 0, 0)).getString(), Constants.PREFIX.get().getString() + "skyblocker.config.mining.crystalsWaypoints.addedWaypointFairy Grotto skyblocker.config.mining.crystalsWaypoints.addedWaypoint.at : 0 0 0.");
    }
}
