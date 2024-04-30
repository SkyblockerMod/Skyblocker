package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class AutopetFilterTest extends ChatPatternListenerTest<AutopetFilter> {
    public AutopetFilterTest() {
        super(new AutopetFilter());
    }

    @Test
    void testAutopet() {
        assertMatches("Autopet equipped your [Lvl 85] Tiger! VIEW RULE");
    }
}