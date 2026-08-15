package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;

class AutopetFilterTest extends ChatPatternListenerTest<AutopetFilter> {
	AutopetFilterTest() {
		super(new AutopetFilter());
	}

	@Test
	void testAutopet() {
		assertMatches("Autopet equipped your [Lvl 85] Tiger! VIEW RULE");
	}
}
