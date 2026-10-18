package de.hysky.skyblocker.skyblock.dwarven;

import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;

class PuzzlerTest extends ChatPatternListenerTest<Puzzler> {
	PuzzlerTest() {
		super(new Puzzler());
	}

	@Test
	void puzzler() {
		assertGroup("[NPC] Puzzler: ◀▲◀▲▲▶▶◀▲▼", 1, "◀▲◀▲▲▶▶◀▲▼");
	}
}
