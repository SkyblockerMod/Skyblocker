package me.xmrvizzy.skyblocker.skyblock.filters;

import org.junit.jupiter.api.Test;

class AoteFilterTest extends ChatFilterTest<AoteFilter> {
    public AoteFilterTest() {
        super(new AoteFilter());
    }

    @Test
    void testRegex() {
        assertMatches("There are blocks in the way!");
    }
}