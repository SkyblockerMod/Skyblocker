package de.hysky.skyblocker.utils;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
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
