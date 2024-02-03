package de.hysky.skyblocker.skyblock.dwarven;

import it.unimi.dsi.fastutil.ints.IntIntPair;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrystalsHudTest {

    @Test
    void testLocationTransformation() {
        Assertions.assertEquals(CrystalsHud.transformLocation(202, 202), IntIntPair.of(0, 0));
        Assertions.assertEquals(CrystalsHud.transformLocation(823, 823), IntIntPair.of(62, 62));

        Assertions.assertEquals(CrystalsHud.transformLocation(512.5, 512.5), IntIntPair.of(31, 31));

        Assertions.assertEquals(CrystalsHud.transformLocation(-50, -50), IntIntPair.of(0, 0));
        Assertions.assertEquals(CrystalsHud.transformLocation(1000, 1000), IntIntPair.of(62, 62));
    }
}
