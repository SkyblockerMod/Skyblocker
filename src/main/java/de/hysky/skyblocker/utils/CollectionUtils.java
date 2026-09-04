package de.hysky.skyblocker.utils;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.Pair;

public final class CollectionUtils {
	private CollectionUtils() {}

	public static <T extends Enum<T>> Collector<T, ?, EnumSet<T>> enumSetCollector(Class<T> elementType) {
		return Collectors.toCollection(() -> EnumSet.noneOf(elementType));
	}

	/// Divides a {@link List} in half at it's midpoint.
	///
	/// @return a {@link Pair} containing the first and second halves of the list.
	///
	/// @implNote The two halves are just sub-lists rather than new {@link List} instances.
	public static <E> Pair<List<E>, List<E>> halve(List<E> list) {
		// Uses ceil div so that the first half is larger than the second half when the element size
		// is odd.
		int mid = Math.ceilDiv(list.size(), 2);

		List<E> firstHalf = list.subList(0, mid);
		List<E> secondHalf = list.subList(mid, list.size());

		return Pair.of(firstHalf, secondHalf);
	}
}
