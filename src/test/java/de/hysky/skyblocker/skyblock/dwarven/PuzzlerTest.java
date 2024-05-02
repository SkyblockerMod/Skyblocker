package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class PuzzlerTest extends ChatPatternListenerTest<Puzzler> {
    public PuzzlerTest() {
        super(new Puzzler());
    }

    @Test
    void puzzler() {
        assertGroup("[NPC] Puzzler: ◀▲◀▲▲▶▶◀▲▼", 1, "◀▲◀▲▲▶▶◀▲▼");
    }
}