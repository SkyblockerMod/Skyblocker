package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

public class TeleportPadFilterTest extends ChatFilterTest<TeleportPadFilter> {
    public TeleportPadFilterTest() {
        super(new TeleportPadFilter());
    }

    @Test
    void testTeleport() {
        assertMatches("Warped from the Base Teleport Pad to the Minion Teleport Pad!");
    }

    @Test
    void testNoDestination() {
        assertMatches("This Teleport Pad does not have a destination set!");
    }
}
