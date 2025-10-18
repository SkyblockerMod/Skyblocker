package de.hysky.skyblocker.utils;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for text operations.
 */
public final class TextUtils {
	private TextUtils() {}

	/**
	 * Finds the first occurrence of the pattern in the list of texts, using {@link Matcher#matches()} ()}.
	 *
	 * @param list the list of texts to search in
	 * @param pattern the pattern to search for
	 * @return the index of the first occurrence if found, -1 otherwise
	 */
	public static int indexOfInList(List<Text> list, Pattern pattern) {
		return indexOfInList(list, pattern, 0);
	}

	/**
	 * Finds the first occurrence of the pattern in the list of texts starting from the specified index, using {@link Matcher#matches()}}.
	 *
	 * @param list the list of texts to search in
	 * @param pattern the pattern to search for
	 * @param startIndex the index to start searching from (inclusive)
	 * @return the index of the first occurrence if found, -1 otherwise
	 */
	public static int indexOfInList(List<Text> list, Pattern pattern, int startIndex) {
		if (startIndex >= list.size()) return -1; // Start index is out of bounds, or the list is empty

		Matcher matcher = pattern.matcher(""); // Empty matcher
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			Text text = list.get(i);
			if (matcher.reset(text.getString()).matches()) return i;
		}

		return -1;
	}

	/**
	 * Finds the first occurrence of the pattern in the list of texts using {@link Matcher#find()}.
	 *
	 * @param list the list of texts to search in
	 * @param pattern the pattern to search for
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static Matcher findInList(List<Text> list, Pattern pattern) {
		return findInList(list, pattern, 0);
	}

	/**
	 * Finds the first occurrence of the pattern in the list of texts starting from the specified index using {@link Matcher#find()}.
	 *
	 * @param list the list of texts to search in
	 * @param pattern the pattern to search for
	 * @param startIndex the index to start searching from (inclusive)
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static Matcher findInList(List<Text> list, Pattern pattern, int startIndex) {
		if (startIndex >= list.size()) return null; // Start index is out of bounds, or the list is empty

		Matcher matcher = pattern.matcher(""); // Empty matcher
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			Text text = list.get(i);
			if (matcher.reset(text.getString()).find()) return matcher;
		}

		return null;
	}

	/**
	 * Finds the first occurrence of the pattern in the list of texts using {@link Matcher#matches()}.
	 *
	 * @param list the list of texts to search in
	 * @param pattern the pattern to search for
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static Matcher matchInList(List<Text> list, Pattern pattern) {
		return matchInList(list, pattern, 0);
	}

	/**
	 * Finds the first occurrence of the pattern in the list of texts starting from the specified index using {@link Matcher#matches()}.
	 *
	 * @param list the list of texts to search in
	 * @param pattern the pattern to search for
	 * @param startIndex the index to start searching from (inclusive)
	 * @return the matcher if found, {@code null} otherwise
	 */
	@Nullable
	public static Matcher matchInList(List<Text> list, Pattern pattern, int startIndex) {
		if (startIndex >= list.size()) return null; // Start index is out of bounds, or the list is empty

		Matcher matcher = pattern.matcher(""); // Empty matcher
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			Text text = list.get(i);
			if (matcher.reset(text.getString()).matches()) return matcher;
		}

		return null;
	}

	/**
	 * Finds the first occurrence of all the patterns in the list of texts using {@link Matcher#matches()} or returns an empty list if any pattern does not match.
	 *
	 * @param list the list of texts to search in
	 * @param patterns the patterns to search for
	 * @return the list of matchers if all patterns matched, an empty list otherwise
	 */
	public static List<Matcher> matchInList(List<Text> list, Pattern... patterns) {
		return matchInList(list, 0, patterns);
	}

	/**
	 * Finds the first occurrence of all the patterns in the list of texts starting from the specified index using {@link Matcher#matches()} or returns an empty list if any pattern does not match.
	 *
	 * @param list the list of texts to search in
	 * @param startIndex the index to start searching from (inclusive)
	 * @param patterns the patterns to search for
	 * @return the list of matchers if all patterns matched, an empty list otherwise
	 */
	public static List<Matcher> matchInList(List<Text> list, int startIndex, Pattern... patterns) {
		if (list.isEmpty()) return List.of();

		List<Matcher> matchers = Arrays.stream(patterns).map(p -> p.matcher("")).toList();
		for (int i = startIndex, listSize = list.size(); i < listSize; i++) {
			String line = list.get(i).getString();
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
