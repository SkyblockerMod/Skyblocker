package me.xmrvizzy.skyblocker.chat.filters;

import org.junit.jupiter.api.Test;

public class ComboFilterTest extends ChatFilterTest<ComboFilter> {
    public ComboFilterTest() {
        super(new ComboFilter());
    }

    @Test
    void testComboMF() {
        assertFilters("+5 Kill Combo +3% ✯ Magic Find");
    }

    @Test
    void testComboCoins() {
        assertFilters("+10 Kill Combo +10 coins per kill");
    }

    @Test
    void testComboExpired() {
        assertFilters("Your Kill Combo has expired! You reached a 11 Kill Combo!");
    }
}
