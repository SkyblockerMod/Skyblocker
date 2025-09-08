package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

public class DungeonBreakerFilterTest extends ChatFilterTest<DungeonBreakerFilter> {
	public DungeonBreakerFilterTest() {
		super(new DungeonBreakerFilter());
	}

	@Test
	void testInsufficientCharges() {
		assertMatches("You don't have enough charges to break this block right now!");
	}

	@Test
	void testUnbreakableBlock() {
		assertMatches("A mystical force prevents you from digging that block!");
	}

	@Test
	void testUnbreakableArea() {
		assertMatches("A mystical force prevents you digging there!");
	}

	@Test
	void testUnbreakableRoom() {
		assertMatches("A mystical force prevents you digging in this room!");
	}
}
