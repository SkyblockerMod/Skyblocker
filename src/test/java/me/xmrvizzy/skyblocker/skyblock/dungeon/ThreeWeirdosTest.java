package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class ThreeWeirdosTest extends ChatListenerTest<ThreeWeirdos> {
    public ThreeWeirdosTest() {
        super(new ThreeWeirdos());
    }

    @Test
    void test1() {
        assertGroup("§e[NPC] §cBaxter§f: My chest doesn't have the reward. We are all telling the truth.", 1, "Baxter");
    }
    @Test
    void test2() {
        assertGroup("§e[NPC] §cHope§f: The reward isn't in any of our chests.", 1, "Hope");
    }
}