package me.xmrvizzy.skyblocker.chat.filters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.List;

class AutopetFilterTest extends ChatFilterTest<AutopetFilter> {
    public AutopetFilterTest() {
        super(new AutopetFilter());
    }

    @Test
    void testAutopet() {
        List<String> expected = List.of("^§cAutopet §eequipped your §7.*§e! §a§lVIEW RULE$");
        List<String> actual = List.of("§cAutopet §eequipped your §7[Lvl 85] §6Tiger§e! §a§lVIEW RULE");

        assertLinesMatch(expected, actual);
    }
}