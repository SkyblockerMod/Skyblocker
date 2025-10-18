package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class AutopetFilterTest extends ChatPatternListenerTest<AutopetFilter> {
    AutopetFilterTest() {
        super(new AutopetFilter());
    }

    @Test
    void testAutopet() {
        assertMatches("Autopet equipped your [Lvl 85] Tiger! VIEW RULE");
    }
}
