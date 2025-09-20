package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

class TriviaTest {
	private static final Pattern PATTERN = Trivia.PATTERN;

	private static void assertGroup(String input, int group, String expected) {
		ChatPatternListenerTest.assertGroup(PATTERN.matcher(input), group, expected);
	}

	@Test
	void anyQuestion1() {
		assertGroup("                      What is the first question?", 1, "What is the first question?");
	}

	@Test
	void anyQuestion2() {
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

	@Test
	void answeredQuestion() {
		String answeredCorrectlyMessage = "[STATUE] Oruo the Omniscient: Player answered Question #2 correctly!";
		assertGroup(answeredCorrectlyMessage, 4, answeredCorrectlyMessage);
	}

	@Test
	void endOfPuzzle() {
		String completionMessage = "[STATUE] Oruo the Omniscient: I bestow upon you all the power of a hundred years!";
		assertGroup(completionMessage, 4, completionMessage);
	}

	@Test
	void puzzleFail() {
		String yikesMessage = "[STATUE] Oruo the Omniscient: Yikes";
		assertGroup(yikesMessage, 4, yikesMessage);
	}
}
