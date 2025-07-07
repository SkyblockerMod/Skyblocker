package de.hysky.skyblocker.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InstancedUtilsTest {

	@SuppressWarnings("EqualsWithItself")
	@Test
	void testSameInstanceEqual() {
		Vector3i vec1 = new Vector3i(8, 8, 8);

		Assertions.assertEquals(vec1, vec1);
	}

	@Test
	void testSameFieldValuesEqual() {
		Vector3i vec1 = new Vector3i(8, 8, 8);
		Vector3i vec2 = new Vector3i(8, 8, 8);

		Assertions.assertEquals(vec1, vec2);
	}

	@Test
	void testDifferentFieldValuesEqual() {
		Vector3i vec1 = new Vector3i(8, 8, 8);
		Vector3i vec2 = new Vector3i(-8, -8, -8);

		Assertions.assertNotEquals(vec1, vec2);
	}

	@Test
	void testHashCodeOfEqualFieldValues() {
		Vector3i vec1 = new Vector3i(8, 8, 8);
		Vector3i vec2 = new Vector3i(8, 8, 8);

		Assertions.assertEquals(vec1.hashCode(), vec2.hashCode());
	}

	@Test
	void testHashCodeOfDifferentFieldValues() {
		Vector3i vec1 = new Vector3i(8, 8, 8);
		Vector3i vec2 = new Vector3i(-8, -8, -8);

		Assertions.assertNotEquals(vec1.hashCode(), vec2.hashCode());
	}

	@Test
	void testToString() {
		Vector3i vec1 = new Vector3i(1, 2, 3);

		Assertions.assertEquals("Vector3i[x=1, y=2, z=3]", vec1.toString());
	}

	@SuppressWarnings("unused")
	private static class Vector3i {
		final int x;
		final int y;
		final int z;

		Vector3i(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(Object o) {
			try {
				return (boolean) InstancedUtils.equals(getClass()).invokeExact(this, o);
			} catch (Throwable ignored) {
				return super.equals(o);
			}
		}

		@Override
		public int hashCode() {
			try {
				return (int) InstancedUtils.hashCode(getClass()).invokeExact(this);
			} catch (Throwable ignored) {
				return System.identityHashCode(this);
			}
		}

		@Override
		public String toString() {
			try {
				return (String) InstancedUtils.toString(getClass()).invokeExact(this);
			} catch (Throwable ignored) {
				return super.toString();
			}
		}
	}
}
