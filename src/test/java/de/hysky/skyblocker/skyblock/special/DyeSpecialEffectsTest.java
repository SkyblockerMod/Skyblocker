package de.hysky.skyblocker.skyblock.special;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DyeSpecialEffectsTest {

	@Test
	void testDye1() {
		Assertions.assertTrue(DyeSpecialEffects.DROP_PATTERN.matcher("WOW! [MVP+] Crystalfall found Necron Dye #7!").matches(), "Dye Test #1 didn't match!");
	}
	
	@Test
	void testDye2() {
		Assertions.assertTrue(DyeSpecialEffects.DROP_PATTERN.matcher("WOW! [MVP+] AzureAaron found Wild Strawberry Dye #1,888!").matches(), "Dye Test #2 didn't match!");
	}

	@Test
	void testDye3() {
		Assertions.assertTrue(DyeSpecialEffects.DROP_PATTERN.matcher("WOW! [MVP+] AzureAaron found Necron Dye!").matches(), "Dye Test #3 didn't match!");
	}

	@Test
	void testDye4() {
		Assertions.assertTrue(DyeSpecialEffects.DROP_PATTERN.matcher("WOW! [MVP+] Viciscat found Cat Dye #1!").matches(), "Dye Test #4 didn't match!");
	}
}
