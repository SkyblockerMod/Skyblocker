package de.hysky.skyblocker.skyblock.filters;

import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;

public class ShowOffFilterTest extends ChatPatternListenerTest<ShowOffFilter> {

	public ShowOffFilterTest() {
		super(new ShowOffFilter());
	}

	@Test
	void holding() {
		assertMatches("[290] ⚡ [MVP+] Player is holding [Withered Dark Claymore ✪✪✪✪✪➎]");
	}

	@Test
	void wearing() {
		assertMatches("[290] ⚡ [MVP+] Player is wearing [Ancient Storm's Chestplate ✪✪✪✪✪➎]");
	}

	@Test
	void isFriendsWith() {
		assertMatches("[290] [MVP+] Player is friends with a [[Lvl 200] Golden Dragon]");
	}

	@Test
	void has() {
		assertMatches("[290] ⚡ [MVP+] Player has [Withered Hyperion ✪✪✪✪✪]");
	}

	@Test
	void noLevelOrEmblem() {
		assertMatches("[MVP+] Player is holding [Mithril Drill SX-R226]");
	}

	@Test
	void noRank() {
		assertMatches("[290] ⚡ Player is holding [Oak Leaves]");
	}

	@Test
	void noLevelOrEmblemOrRank() {
		assertMatches("Player is holding [Nether Star]");
	}
}
