package de.hysky.skyblocker.skyblock.garden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GreenhousePasteTest {
	@Test
	void extractLayoutCode() {
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/?layout=ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://api.skyshards.com/share/ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("  ABC  "));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?tab=1&layout=ABC&mode=x"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://api.skyshards.com/share/ABC?foo=bar"));
		Assertions.assertEquals("a-b_c=", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=a-b_c="));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://skymutations.eu/?layout=ABC"));
	}
}
