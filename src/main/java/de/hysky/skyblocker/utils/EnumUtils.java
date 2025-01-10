package de.hysky.skyblocker.utils;

public class EnumUtils {
	public static <T extends Enum<T>> T cycle(T current) {
		T[] values = current.getDeclaringClass().getEnumConstants();
		return values[(current.ordinal() + 1) % values.length];
	}

	public static <T extends Enum<T>> T cycleBackwards(T current) {
		T[] values = current.getDeclaringClass().getEnumConstants();
		return values[(current.ordinal() - 1 + values.length) % values.length];
	}
}
