package me.xmrvizzy.skyblocker.chat.filters;

import org.junit.jupiter.api.Test;

public class TeleportPadFilterTest extends ChatFilterTest<TeleportPadFilter> {
    public TeleportPadFilterTest() {
        super(new TeleportPadFilter());
    }

    @Test
    void testTeleport() {
        assertFilters("Warped from the Base Teleport Pad to the Minion Teleport Pad!");
    }

    @Test
    void testNoDestination() {
        assertFilters("This Teleport Pad does not have a destination set!");
    }
}