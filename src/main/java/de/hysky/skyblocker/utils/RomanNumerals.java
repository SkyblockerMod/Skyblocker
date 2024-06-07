package de.hysky.skyblocker.utils;

public class RomanNumerals {
	private RomanNumerals() {
	}
	private static int getDecimalValue(char romanChar) {
		return switch (romanChar) {
			case 'I' -> 1;
			case 'V' -> 5;
			case 'X' -> 10;
			case 'L' -> 50;
			case 'C' -> 100;
			case 'D' -> 500;
			case 'M' -> 1000;
			default -> 0;
		};
	}

	/**
	 * Checks if a string is a valid roman numeral.
	 * It's the caller's responsibility to clean up the string before calling this method (such as trimming it).
	 * @param romanNumeral The roman numeral to check.
	 * @return True if the string is a valid roman numeral, false otherwise.
	 * @implNote This will only check if the string contains valid roman numeral characters. It won't check if the numeral is well-formed.
	 */
	public static boolean isValidRomanNumeral(String romanNumeral) {
		if (romanNumeral == null || romanNumeral.isEmpty()) return false;
		for (int i = 0; i < romanNumeral.length(); i++) {
			if (getDecimalValue(romanNumeral.charAt(i)) == 0) return false;
		}
		return true;
	}

	/**
	 * Converts a roman numeral to a decimal number.
	 *
	 * @param romanNumeral The roman numeral to convert.
	 * @return The decimal number, or 0 if the roman numeral string has non-roman characters in it, or if the string is empty or null.
	 */
	public static int romanToDecimal(String romanNumeral) {
		if (romanNumeral == null || romanNumeral.isEmpty()) return 0;
		romanNumeral = romanNumeral.trim().toUpperCase();
		int decimal = 0;
		int lastNumber = 0;
		for (int i = romanNumeral.length() - 1; i >= 0; i--) {
			char ch = romanNumeral.charAt(i);
			int number = getDecimalValue(ch);
			if (number == 0) return 0; //Malformed roman numeral
			decimal = number >= lastNumber ? decimal + number : decimal - number;
			lastNumber = number;
		}
		return decimal;
	}
}
