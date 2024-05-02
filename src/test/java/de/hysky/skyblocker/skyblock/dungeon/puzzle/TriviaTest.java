package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
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
        assertGroup("     ⓐ Answer 1", 3, "Answer 1");
    }
    @Test
    void answer2() {
        assertGroup("     ⓑ Answer 2", 3, "Answer 2");
    }
    @Test
    void answer3() {
        assertGroup("     ⓒ Answer 3", 3, "Answer 3");
    }
}