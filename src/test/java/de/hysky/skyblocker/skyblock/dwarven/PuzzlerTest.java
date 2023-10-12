package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class PuzzlerTest extends ChatPatternListenerTest<Puzzler> {
    public PuzzlerTest() {
        super(new Puzzler());
    }

    @Test
    void puzzler() {
        assertGroup("§e[NPC] §dPuzzler§f: §b◀§d▲§b◀§d▲§d▲§5▶§5▶§b◀§d▲§a▼", 1, "§b◀§d▲§b◀§d▲§d▲§5▶§5▶§b◀§d▲§a▼");
    }
}