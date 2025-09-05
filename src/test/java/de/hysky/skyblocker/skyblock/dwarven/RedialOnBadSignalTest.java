package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

public class RedialOnBadSignalTest extends ChatPatternListenerTest<RedialOnBadSignal> {
	RedialOnBadSignalTest() {
		super(new RedialOnBadSignal());
	}

	@Test
	void badSignals() {
		// https://wiki.hypixel.net/Fred#Dialogue
		assertGroup("[NPC] Fred: ✆ I'm not interested in extending my anvil warranty.", 1, "Fred");
		assertGroup("[NPC] Fred: ✆ All I hear is crackling, the signal is really bad down here.", 1, "Fred");
		assertGroup("[NPC] Fred: ✆ What are you saying, the forge is in the omelette???", 1, "Fred");
		assertGroup("[NPC] Fred: ✆ You're breaking up, I CAN'T HEAR YOU!", 1, "Fred");
	}
}
