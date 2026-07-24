package de.hysky.skyblocker.skyblock.dwarven.fossil;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class FossilMuncherTest extends ChatPatternListenerTest<FossilMuncher> {
	FossilMuncherTest() {
		super(new FossilMuncher());
	}

	@Test
	public void patternCaptures() {
		assertGroup("[NPC] Fossil Muncher: the fossil i want is a hint", 1, "is a hint");
	}
}
