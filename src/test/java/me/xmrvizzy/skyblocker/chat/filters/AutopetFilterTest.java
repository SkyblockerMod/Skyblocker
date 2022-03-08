package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.List;

class AutopetFilterTest extends ChatPatternListenerTest<AutopetFilter> {
    public AutopetFilterTest() {
        super(new AutopetFilter());
    }

    @Test
    void testAutopet() {
        List<String> expected = List.of(listener.pattern.toString());
        List<String> actual = List.of("§cAutopet §eequipped your §7[Lvl 85] §6Tiger§e! §a§lVIEW RULE");

        assertLinesMatch(expected, actual);
    }
}