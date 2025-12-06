package de.hysky.skyblocker.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for text operations.
 */
public final class RegexListUtils {
	private RegexListUtils() {}

	/**
	 * Finds the first occurrence of the pattern in the list of strings, using {@link Matcher#matches()}.
	 *
	 * @param list    the list of strings to search in
	 * @param pattern the pattern to search for
	 * @return the index of the first occurrence if found, -1 otherwise
	 */
	public static int indexOfInList(List<String> list, Pattern pattern) {
		return indexOfInList(list, Function.identity(), pattern);
	}

	/**
	 * Finds the first occurrence of the pattern in the list, using {@link Matcher#matches()}.
	 *
	 * @param list             the list to search in
	 * @param toStringFunction the function to convert list elements to strings
	 * @param pattern          the pattern to search for
	 * @return the index of the first occurrence if found, -1 otherwise
	 */
	public static <T> int indexOfInList(List<T> list, Function<T, String> toStringFunction, Pattern pattern) {
		return indexOfInList(list, toStringFunction, pattern, 0);
	}

	/**
	 * Finds the first occurrence of the pattern in the list starting from the specified index, using {@link Matcher#matches()}}.
	 *
	 * @param list             the list to search in
	 * @param toStringFunction the function to convert list elements to strings
	 * @param pattern          the pattern to search for
	 * @param startIndex       the index to start searching from (inclusive)
	 * @return the index of the first occurrence if found, -1 otherwise
	 */
	public static <T> int indexOfInList(List<T> list, Function<T, String> toStringFunction, Pattern pattern, int startIndex) {
		if (startIndex >= list.size()) return -1; // Start index is out of bounds, or the list is empty

		Matcher matcher = pattern.matcher(""); // Empty matcher
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			String string = toStringFunction.apply(list.get(i));
			if (matcher.reset(string).matches()) return i;
		}

		return -1;
	}

	/**
	 * Finds the first occurrence of the pattern in the list of strings using {@link Matcher#find()}.
	 *
	 * @param list    the list of strings to search in
	 * @param pattern the pattern to search for
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static Matcher findInList(List<String> list, Pattern pattern) {
		return findInList(list, Function.identity(), pattern);
	}

	/**
	 * Finds the first occurrence of the pattern in the list using {@link Matcher#find()}.
	 *
	 * @param list             the list to search in
	 * @param toStringFunction the function to convert list elements to strings
	 * @param pattern          the pattern to search for
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static <T> Matcher findInList(List<T> list, Function<T, String> toStringFunction, Pattern pattern) {
		return findInList(list, toStringFunction, pattern, 0);
	}

	/**
	 * Finds the first occurrence of the pattern in the list starting from the specified index using {@link Matcher#find()}.
	 *
	 * @param list             the list to search in
	 * @param toStringFunction the function to convert list elements to strings
	 * @param pattern          the pattern to search for
	 * @param startIndex       the index to start searching from (inclusive)
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static <T> Matcher findInList(List<T> list, Function<T, String> toStringFunction, Pattern pattern, int startIndex) {
		if (startIndex >= list.size()) return null; // Start index is out of bounds, or the list is empty

		Matcher matcher = pattern.matcher(""); // Empty matcher
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			String string = toStringFunction.apply(list.get(i));
			if (matcher.reset(string).find()) return matcher;
		}

		return null;
	}

	/**
	 * Finds the first occurrence of the pattern in the list of strings using {@link Matcher#matches()}.
	 *
	 * @param list    the list of strings to search in
	 * @param pattern the pattern to search for
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static Matcher matchInList(List<String> list, Pattern pattern) {
		return matchInList(list, Function.identity(), pattern);
	}

	/**
	 * Finds the first occurrence of the pattern in the list using {@link Matcher#matches()}.
	 *
	 * @param list             the list to search in
	 * @param toStringFunction the function to convert list elements to strings
	 * @param pattern          the pattern to search for
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static <T> Matcher matchInList(List<T> list, Function<T, String> toStringFunction, Pattern pattern) {
		return matchInList(list, toStringFunction, pattern, 0);
	}

	/**
	 * Finds the first occurrence of the pattern in the list starting from the specified index using {@link Matcher#matches()}.
	 *
	 * @param list       the list to search in
	 * @param pattern    the pattern to search for
	 * @param startIndex the index to start searching from (inclusive)
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static <T> Matcher matchInList(List<T> list, Function<T, String> toStringFunction, Pattern pattern, int startIndex) {
		if (startIndex >= list.size()) return null; // Start index is out of bounds, or the list is empty

		Matcher matcher = pattern.matcher(""); // Empty matcher
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			String string = toStringFunction.apply(list.get(i));
			if (matcher.reset(string).matches()) return matcher;
		}

		return null;
	}

	/**
	 * Finds the first occurrence of all the patterns in the list of strings using {@link Matcher#matches()} or returns an empty list if any pattern does not match.
	 *
	 * @param list     the list of strings to search in
	 * @param patterns the patterns to search for
	 * @return the list of matchers if all patterns matched, an empty list otherwise
	 */
	public static List<Matcher> matchInList(List<String> list, Pattern... patterns) {
		return matchInList(list, Function.identity(), patterns);
	}

	/**
	 * Finds the first occurrence of all the patterns in the list using {@link Matcher#matches()} or returns an empty list if any pattern does not match.
	 *
	 * @param list             the list to search in
	 * @param toStringFunction the function toconvert list elements to strings
	 * @param patterns         the patterns to search for
	 * @return the list ofmatchers if all patterns matched, an empty list otherwise
	 */
	public static <T> List<Matcher> matchInList(List<T> list, Function<T, String> toStringFunction, Pattern... patterns) {
		return matchInList(list, toStringFunction, 0, patterns);
	}

	/**
	 * Finds the first occurrence of all the patterns in the list starting from the specified index using {@link Matcher#matches()} or returns an empty list if any pattern does not match.
	 *
	 * @param list       the list to search in
	 * @param startIndex the index to start searching from (inclusive)
	 * @param patterns   the patterns to search for
	 * @return the list of matchers if all patterns matched, an empty list otherwise
	 */
	public static <T> List<Matcher> matchInList(List<T> list, Function<T, String> toStringFunction, int startIndex, Pattern... patterns) {
		if (list.isEmpty()) return List.of();

		List<Matcher> matchers = Arrays.stream(patterns).map(p -> p.matcher("")).toList();
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			String line = toStringFunction.apply(list.get(i));
			boolean allMatched = true;
			for (Matcher matcher : matchers) {
				if (!matcher.hasMatch() && !matcher.reset(line).matches()) {
					allMatched = false;
				}
			}
			if (allMatched) return matchers;
		}

		return List.of();
	}
}
