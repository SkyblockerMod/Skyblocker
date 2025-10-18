package de.hysky.skyblocker.utils.test;

import de.hysky.skyblocker.annotations.GenEquals;
import de.hysky.skyblocker.annotations.GenHashCode;
import de.hysky.skyblocker.annotations.GenToString;

/**
 * Test utilities for testing the capabilities of {@link GenEquals}, {@link GenHashCode}, {@link GenToString} as
 * the processors do not apply to the test source set.
 */
public class ObjectMethodGenerationTestUtils {
	public static class Point2D {
		public final int x;
		public final int y;
		public transient long testTransient;

		public Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		@GenEquals
		public native boolean equals(Object o);

		@Override
		@GenHashCode
		public native int hashCode();

		@Override
		@GenToString
		public native String toString();
	}

	public static class Point3DWithSuper extends Point2D {
		public final int z;

		public Point3DWithSuper(int x, int y, int z) {
			super(x, y);
			this.z = z;
		}

		@Override
		@GenEquals(includeSuper = true)
		public native boolean equals(Object o);

		@Override
		@GenHashCode(includeSuper = true)
		public native int hashCode();

		@Override
		@GenToString
		public native String toString();
	}

	public static class Point3DWithoutSuper extends Point2D {
		public final int z;

		public Point3DWithoutSuper(int x, int y, int z) {
			super(x, y);
			this.z = z;
		}

		@Override
		@GenEquals
		public native boolean equals(Object o);

		@Override
		@GenHashCode
		public native int hashCode();

		@Override
		@GenToString
		public native String toString();
	}
}
