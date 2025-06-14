package de.hysky.skyblocker.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class RegexUtils {
	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	public static OptionalLong findLongFromMatcher(Matcher matcher) {
		return findLongFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	public static OptionalLong findLongFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalLong.empty();
		return OptionalLong.of(Long.parseLong(matcher.group(1).replace(",", "")));
	}

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	public static OptionalInt findIntFromMatcher(Matcher matcher) {
		return findIntFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	public static OptionalInt findIntFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalInt.empty();
		return OptionalInt.of(parseIntFromMatcher(matcher, 1));
	}

	/**
	 * Tries to {@link Matcher#find()} a match in the matcher, and parses the first group as a roman numeral.
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 * @implNote Hypixel generally has optional roman numerals in the case of level 0, so this method will return a 0 if the matcher finds a match with an empty first group.
	 * 		Example pattern: {@code Level ?([IVXLCDM]+)?}
	 */
	public static OptionalInt findRomanNumeralFromMatcher(Matcher matcher) {
		return findRomanNumeralFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * Tries to {@link Matcher#find()} a match in the matcher from a starting index, and parses the first group as a roman numeral.
	 * @return An OptionalInt of the first group in the matcher after parsing via {@link RomanNumerals#romanToDecimal(String)}, or an empty OptionalInt if the matcher doesn't find anything / finds invalid roman numerals.
	 * @implNote Hypixel generally has optional roman numerals in the case of level 0, so this method will return a 0 if the matcher finds a match with an empty first group.
	 * 		Example pattern: {@code Level ?([IVXLCDM]+)?}
	 */
	public static OptionalInt findRomanNumeralFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalInt.empty();
		String result = matcher.group(1);
		if (StringUtils.isEmpty(result)) return OptionalInt.of(0); // Special case for level 0
		if (!RomanNumerals.isValidRomanNumeral(result)) return OptionalInt.empty();
		int resultInt = RomanNumerals.romanToDecimal(result);
		if (resultInt <= 0) return OptionalInt.empty(); // This shouldn't happen since we checked above, but just in case.
		return OptionalInt.of(resultInt);
	}

	public static OptionalInt parseOptionalIntFromMatcher(Matcher matcher, int group) {
		String s = matcher.group(group);
		if (s == null) return OptionalInt.empty();
		return OptionalInt.of(Integer.parseInt(s.replace(",", "")));
	}

	public static OptionalInt parseOptionalIntFromMatcher(MatchResult matcher, String group) {
		String s = matcher.group(group);
		if (s == null) return OptionalInt.empty();
		return OptionalInt.of(Integer.parseInt(s.replace(",", "")));
	}

	public static int parseIntFromMatcher(Matcher matcher, int group) {
		return Integer.parseInt(matcher.group(group).replace(",", ""));
	}

	public static int parseIntFromMatcher(Matcher matcher, String group) {
		return Integer.parseInt(matcher.group(group).replace(",", ""));
	}

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	public static OptionalDouble findDoubleFromMatcher(Matcher matcher) {
		return findDoubleFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	public static OptionalDouble findDoubleFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalDouble.empty();
		return OptionalDouble.of(Double.parseDouble(matcher.group(1).replace(",", "")));
	}
}
