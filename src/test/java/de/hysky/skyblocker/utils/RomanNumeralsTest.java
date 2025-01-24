package de.hysky.skyblocker.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RomanNumeralsTest {
	@Test
	void testRomanToDecimal() {
		// Test the first 50 numbers
		String[] expected = new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX", "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX", "XL", "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX", "L"};
		for (int i = 1; i <= 50; i++) {
			Assertions.assertEquals(i, RomanNumerals.romanToDecimal(expected[i - 1]));
		}
		Assertions.assertEquals(100, RomanNumerals.romanToDecimal("C"));
		Assertions.assertEquals(400, RomanNumerals.romanToDecimal("CD"));
		Assertions.assertEquals(500, RomanNumerals.romanToDecimal("D"));
		Assertions.assertEquals(900, RomanNumerals.romanToDecimal("CM"));
		Assertions.assertEquals(1000, RomanNumerals.romanToDecimal("M"));
		Assertions.assertEquals(1999, RomanNumerals.romanToDecimal("MCMXCIX"));
	}

	@Test
	void isValidRoman() {
		Assertions.assertTrue(RomanNumerals.isValidRomanNumeral("I"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral("AI"));
		Assertions.assertTrue(RomanNumerals.isValidRomanNumeral("CMI"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral(" CMI"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral("XI I"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral("A"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral("15"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral("MCMLXXXAIV"));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral(null));
		Assertions.assertFalse(RomanNumerals.isValidRomanNumeral(""));
	}

	@Test
	void testDecimalToRoman() {
		String[] expected = new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX", "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX", "XL", "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX", "L"};
		for (int i = 1; i <= 50; i++) {
			Assertions.assertEquals(expected[i - 1], RomanNumerals.decimalToRoman(i));
		}
		Assertions.assertEquals("C", RomanNumerals.decimalToRoman(100));
		Assertions.assertEquals("CD", RomanNumerals.decimalToRoman(400));
		Assertions.assertEquals("D", RomanNumerals.decimalToRoman(500));
		Assertions.assertEquals("CM", RomanNumerals.decimalToRoman(900));
		Assertions.assertEquals("M", RomanNumerals.decimalToRoman(1000));
		Assertions.assertEquals("MCMXCIX", RomanNumerals.decimalToRoman(1999));
	}
}
