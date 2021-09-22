package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class PuzzlerTest extends ChatListenerTest<Puzzler> {
    public PuzzlerTest() {
        super(new Puzzler());
    }

    @Test
    void puzzler() {
        assertGroup("§e[NPC] §dPuzzler§f: §b◀§d▲§b◀§d▲§d▲§5▶§5▶§b◀§d▲§a▼", 1, "§b◀§d▲§b◀§d▲§d▲§5▶§5▶§b◀§d▲§a▼");
    }
}