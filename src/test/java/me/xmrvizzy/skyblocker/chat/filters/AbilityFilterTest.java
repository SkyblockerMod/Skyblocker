package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class AbilityFilterTest extends ChatFilterTest<AbilityFilter> {
    public AbilityFilterTest() {
        super(new AbilityFilter());
    }

    @Test
    void charges() {
        assertFilters("No more charges, next one in 13.2s!");
    }

    @Test
    void cooldown() {
        assertFilters("This ability is on cooldown for 42s.");
    }
}