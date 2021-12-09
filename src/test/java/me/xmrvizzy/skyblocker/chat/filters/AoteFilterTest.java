package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class AoteFilterTest extends ChatFilterTest<AoteFilter> {
    public AoteFilterTest() {
        super(new AoteFilter());
    }

    @Test
    void testRegex() {
        assertFilters("There are blocks in the way!");
    }
}