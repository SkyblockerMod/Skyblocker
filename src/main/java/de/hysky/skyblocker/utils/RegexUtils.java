package de.hysky.skyblocker.utils;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.Matcher;

public class RegexUtils {
	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	public static OptionalLong getLongFromMatcher(Matcher matcher) {
		return getLongFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	public static OptionalLong getLongFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalLong.empty();
		return OptionalLong.of(Long.parseLong(matcher.group(1).replace(",", "")));
	}

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	public static OptionalInt getIntFromMatcher(Matcher matcher) {
		return getIntFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	public static OptionalInt getIntFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalInt.empty();
		return OptionalInt.of(Integer.parseInt(matcher.group(1).replace(",", "")));
	}

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	public static OptionalDouble getDoubleFromMatcher(Matcher matcher) {
		return getDoubleFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	public static OptionalDouble getDoubleFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalDouble.empty();
		return OptionalDouble.of(Double.parseDouble(matcher.group(1).replace(",", "")));
	}
}
