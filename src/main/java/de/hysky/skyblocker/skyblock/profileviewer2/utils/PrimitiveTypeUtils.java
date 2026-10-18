package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import org.jspecify.annotations.Nullable;

public class PrimitiveTypeUtils {

	/// Attempts to convert the given {@code object} into an integer, defaulting to 0.
	public static int coerceInt(@Nullable Object object) {
		return object instanceof Number number ? number.intValue() : 0;
	}

	/// Attempts to convert the given {@code object} into a double, defaulting to 0.
	public static double coerceDouble(@Nullable Object object) {
		return object instanceof Number number ? number.doubleValue() : 0d;
	}

	/// Attempts to convert the given {@code object} into a boolean, defaulting to false.
	public static boolean coerceBoolean(@Nullable Object object) {
		return object instanceof Boolean bool ? bool : false;
	}
}
