package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

public class SkyMallFilterTest extends ChatFilterTest<SkyMallFilter> {

	SkyMallFilterTest() {
		super(new SkyMallFilter());
	}

	@Test
	void testBuffChanged() {
		assertMatches("New day! Your Sky Mall buff changed!");
	}

	@Test
	void testBuff() {
		assertMatches("New buff: Gain +100⸕ Mining Speed.");
		assertMatches("New buff: Gain +50☘ Mining Fortune.");
		assertMatches("New buff: Gain +15% more Powder while mining.");
		assertMatches("New buff: Gain -20% Pickaxe Ability cooldowns.");
		assertMatches("New buff: Gain 10x chance to find Golden and Diamond Goblins.");
		assertMatches("New buff: Gain 5x Titanium drops.");
	}

	@Test
	void testToggleSkyMall() {
		assertMatches("You can disable this messaging by toggling Sky Mall in your /hotm!");
	}
}
