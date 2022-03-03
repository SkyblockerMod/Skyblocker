package me.xmrvizzy.skyblocker.chat.filters;

import org.junit.jupiter.api.Test;

class AutopetFilterTest extends ChatFilterTest<AutopetFilter> {
    public AutopetFilterTest() {
        super(new AutopetFilter());
    }

    @Test
    void testAutopet() {
        assertFilters("§cAutopet §eequipped your §7[Lvl 85] §6Tiger§e! §a§lVIEW RULE");
    }
}