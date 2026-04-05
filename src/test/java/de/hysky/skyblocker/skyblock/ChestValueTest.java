package de.hysky.skyblocker.skyblock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChestValueTest {
	@Test
	void testProfitText() {
		Assertions.assertEquals("literal{ 0 Coins}[style={color=dark_gray}]", ChestValue.getProfitText(0, false).toString());
		Assertions.assertEquals("literal{ 0 Coins}[style={color=blue}]", ChestValue.getProfitText(0, true).toString());
		Assertions.assertEquals("literal{ +10,000 Coins}[style={color=dark_green}]", ChestValue.getProfitText(10000, false).toString());
		Assertions.assertEquals("literal{ +10,000 Coins}[style={color=blue}]", ChestValue.getProfitText(10000, true).toString());
		Assertions.assertEquals("literal{ -10,000 Coins}[style={color=red}]", ChestValue.getProfitText(-10000, false).toString());
		Assertions.assertEquals("literal{ -10,000 Coins}[style={color=blue}]", ChestValue.getProfitText(-10000, true).toString());
		Assertions.assertEquals("literal{ 10,000 Coins}[style={color=dark_green}]", ChestValue.getValueText(10000, false).toString());
		Assertions.assertEquals("literal{ 10,000 Coins}[style={color=blue}]", ChestValue.getValueText(10000, true).toString());
	}
}
