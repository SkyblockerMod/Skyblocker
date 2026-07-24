package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;
import de.hysky.skyblocker.utils.SkyBlockIcons;

public class ComboFilterTest extends ChatFilterTest<ComboFilter> {
	public ComboFilterTest() {
		super(new ComboFilter());
	}

	@Test
	void testComboMF() {
		assertMatches(String.format("+5 Kill Combo +3%% %s Magic Find", SkyBlockIcons.MAGIC_FIND));
	}

	@Test
	void testComboCoins() {
		assertMatches("+10 Kill Combo +10 coins per kill");
	}

	@Test
	void testComboWisdom() {
		assertMatches("+20 Kill Combo +15☯ Combat Wisdom");
	}

	@Test
	void testComboNoBonus() {
		assertMatches("+50 Kill Combo");
	}

	@Test
	void testComboExpired() {
		assertMatches("Your Kill Combo has expired! You reached a 11 Kill Combo!");
	}
}
