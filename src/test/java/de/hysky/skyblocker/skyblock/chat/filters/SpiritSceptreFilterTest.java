package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

public class SpiritSceptreFilterTest extends ChatFilterTest<SpiritSceptreFilter> {
	SpiritSceptreFilterTest() {
		super(new SpiritSceptreFilter());
	}

	@Test
	void oneEnemy() {
		assertMatches("Your Spirit Sceptre hit 1 enemy for 47,253.4 damage.");
	}

	@Test
	void multipleEnemies() {
		assertMatches("Your Spirit Sceptre hit 3 enemies for 141,760.1 damage.");
	}
}
