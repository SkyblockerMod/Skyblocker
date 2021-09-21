package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class TriviaTest extends ChatListenerTest<Trivia> {
    public TriviaTest() {
        super(new Trivia());
    }

    @Test
    void question() {
        assertGroup("                       What is the status of Necron?", 1, "What is the status of Necron?");
    }

    @Test
    void qestion2() {
        assertGroup("       How many Fairy Souls are there in Jerry's Workshop?", 1, "How many Fairy Souls are there in Jerry's Workshop?");
    }

    @Test
    void answer() {
        assertGroup("     §6 ⓑ §a9 Fairy Souls", 3, "9 Fairy Souls");
    }
}