package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class TriviaTest extends ChatPatternListenerTest<Trivia> {
    public TriviaTest() {
        super(new Trivia());
    }

    @Test
    void anyQuestion1() {
        assertGroup("                      What is the first question?", 1, "What is the first question?");
    }

    @Test
    void anyQestion2() {
        assertGroup("      How many questions are there?", 1, "How many questions are there?");
    }

    @Test
    void answer1() {
        assertGroup("    §6 ⓐ §aAnswer 1", 3, "Answer 1");
    }

    @Test
    void answer2() {
        assertGroup("    §6 ⓑ §aAnswer 2", 3, "Answer 2");
    }

    @Test
    void answer3() {
        assertGroup("    §6 ⓒ §aAnswer 3", 3, "Answer 3");
    }
}