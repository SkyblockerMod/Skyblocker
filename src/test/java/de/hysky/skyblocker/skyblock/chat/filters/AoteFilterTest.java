package de.hysky.skyblocker.skyblock.chat.filters;

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