package de.hysky.skyblocker.skyblock.filters;

import org.junit.jupiter.api.Test;

public class ComboFilterTest extends ChatFilterTest<ComboFilter> {
    public ComboFilterTest() {
        super(new ComboFilter());
    }

    @Test
    void testComboMF() {
        assertMatches("+5 Kill Combo +3% ✯ Magic Find");
    }

    @Test
    void testComboCoins() {
        assertMatches("+10 Kill Combo +10 coins per kill");
    }

    @Test
    void testComboEXP() {
        assertMatches("+20 Kill Combo +15% Combat Exp");
    }

    @Test
    void testComboExpired() {
        assertMatches("Your Kill Combo has expired! You reached a 11 Kill Combo!");
    }
}
