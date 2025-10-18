package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

class AbilityFilterTest extends ChatFilterTest<AbilityFilter> {
    AbilityFilterTest() {
        super(new AbilityFilter());
    }

    @Test
    void charges() {
        assertMatches("No more charges, next one in 13.2s!");
    }

    @Test
    void cooldown() {
        assertMatches("This ability is on cooldown for 42s.");
    }
}
