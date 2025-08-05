package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

public class LotteryFilterTest extends ChatFilterTest<LotteryFilter> {

	LotteryFilterTest() {
		super(new LotteryFilter());
	}

	@Test
	void testBuffChanged() {
		assertMatches("New day! Your Lottery buff changed!");
	}

	@Test
	void testBuff() {
		assertMatches("New buff: Gain +50☘ Fig Fortune.");
		assertMatches("New buff: Gain +50☘ Mangrove Fortune.");
		assertMatches("New buff: Gain +5% \u222E Sweep.");
	}

	@Test
	void testLottery() {
		assertMatches("You can disable this messaging by toggling Lottery in your /hotf!");
	}
}
