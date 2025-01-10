package de.hysky.skyblocker.skyblock.dwarven;

import org.joml.Vector2i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrystalsHudTest {

    @Test
    void testLocationTransformation() {
        Assertions.assertEquals(CrystalsHudWidget.transformLocation(202, 202), new Vector2i(0, 0));
        Assertions.assertEquals(CrystalsHudWidget.transformLocation(823, 823), new Vector2i(62, 62));

        Assertions.assertEquals(CrystalsHudWidget.transformLocation(512.5, 512.5), new Vector2i(31, 31));

        Assertions.assertEquals(CrystalsHudWidget.transformLocation(-50, -50), new Vector2i(0, 0));
        Assertions.assertEquals(CrystalsHudWidget.transformLocation(1000, 1000), new Vector2i(62, 62));
    }
}
