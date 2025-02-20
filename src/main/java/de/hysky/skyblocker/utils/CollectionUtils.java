package de.hysky.skyblocker.utils;

import java.util.EnumSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class CollectionUtils {
	private CollectionUtils() {}

	public static <T extends Enum<T>> Collector<T, ?, EnumSet<T>> enumSetCollector(Class<T> elementType) {
		return Collectors.toCollection(() -> EnumSet.noneOf(elementType));
	}
}
