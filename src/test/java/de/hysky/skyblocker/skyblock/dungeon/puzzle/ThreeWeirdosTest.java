package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class ThreeWeirdosTest {
    @Test
    void testBaxter() {
        ChatPatternListenerTest.assertGroup(ThreeWeirdos.PATTERN.matcher("[NPC] Baxter: My chest doesn't have the reward. We are all telling the truth."), 1, "Baxter");
    }

    @Test
    void testHope() {
        ChatPatternListenerTest.assertGroup(ThreeWeirdos.PATTERN.matcher("[NPC] Hope: The reward isn't in any of our chests."), 1, "Hope");
    }
}
