package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FetchurTest extends ChatListenerTest<Fetchur> {
    public FetchurTest() {
        super(new Fetchur());
    }

    @Test
    public void patternCaptures() {
        assertEquals(getGroups("§e[NPC] Fetchur§f: its a hint")[1], "a hint");
    }
}
