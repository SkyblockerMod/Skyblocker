package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class AutopetFilterTest extends ChatPatternListenerTest<AutopetFilter> {
    public AutopetFilterTest() {
        super(new AutopetFilter());
    }

    @Test
    void testAutopet() {
        assertMatches("§cAutopet §eequipped your §7[Lvl 85] §6Tiger§e! §a§lVIEW RULE");
    }
}