package de.hysky.skyblocker.skyblock.filters;

import org.junit.jupiter.api.Test;

class HealFilterTest extends ChatFilterTest<HealFilter> {
    public HealFilterTest() {
        super(new HealFilter());
    }

    @Test
    void healSelf() {
        assertMatches("You healed yourself for 18.3 health!");
    }

    @Test
    void healedYou() {
        assertMatches("H3aler_ healed you for 56 health!");
    }
}