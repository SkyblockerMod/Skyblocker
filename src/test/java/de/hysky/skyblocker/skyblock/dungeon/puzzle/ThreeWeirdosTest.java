package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class ThreeWeirdosTest extends ChatPatternListenerTest<ThreeWeirdos> {
    public ThreeWeirdosTest() {
        super(new ThreeWeirdos());
    }

    @Test
    void test1() {
        assertGroup("[NPC] Baxter: My chest doesn't have the reward. We are all telling the truth.", 1, "Baxter");
    }
    @Test
    void test2() {
        assertGroup("[NPC] Hope: The reward isn't in any of our chests.", 1, "Hope");
    }
}