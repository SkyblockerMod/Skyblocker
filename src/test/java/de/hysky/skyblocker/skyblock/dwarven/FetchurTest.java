package de.hysky.skyblocker.skyblock.dwarven;

import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;

class FetchurTest extends ChatPatternListenerTest<Fetchur> {
	FetchurTest() {
		super(new Fetchur());
	}

	@Test
	public void patternCaptures() {
		assertGroup("[NPC] Fetchur: its a hint", 1, "a hint");
	}
}
