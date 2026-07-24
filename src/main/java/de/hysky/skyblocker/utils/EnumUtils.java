package de.hysky.skyblocker.utils;

import java.util.List;

public class EnumUtils {
	public static <T extends Enum<T>> T cycle(T current) {
		T[] values = current.getDeclaringClass().getEnumConstants();
		return values[(current.ordinal() + 1) % values.length];
	}

	public static <T extends Enum<T>> T cycleBackwards(T current) {
		T[] values = current.getDeclaringClass().getEnumConstants();
		return values[(current.ordinal() - 1 + values.length) % values.length];
	}

	/**
	 * I know this has nothing to do with Enums but it is similar in nature to the above and there was
	 * no where else to stick it. Maybe a rename of this class is in order later down the road?
	 */
	public static <T> T cycle(List<T> list, int currentIndex) {
		return list.get((currentIndex + 1) % list.size());
	}
}
