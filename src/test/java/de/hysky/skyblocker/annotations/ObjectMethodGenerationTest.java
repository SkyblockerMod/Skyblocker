package de.hysky.skyblocker.annotations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.test.ObjectMethodGenerationTestUtils;

public class ObjectMethodGenerationTest {

	@Test
	void testEqualsWithTransientIgnored() {
		var pointA = new ObjectMethodGenerationTestUtils.Point2D(100, 100);
		pointA.testTransient = 10000L;
		var pointB = new ObjectMethodGenerationTestUtils.Point2D(100, 100);
		pointB.testTransient = -10000L;

		Assertions.assertEquals(pointA, pointB);
	}

	@Test
	void testSameInstanceEqual() {
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);

		Assertions.assertEquals(pointA, pointA);
	}

	@Test
	void testEqualsWithoutSuper() {
		//Only the z field (last parameter) is considered
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 100, 200);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 200, 200);
		var pointC = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(200, 100, 200);

		//Test all combinations
		Assertions.assertEquals(pointA, pointB);
		Assertions.assertEquals(pointB, pointC);
		Assertions.assertEquals(pointC, pointA);
	}

	@Test
	void testNotEqualsWithoutSuper() {
		//Only the z field (last parameter) is considered
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 100, 100);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 100, 200);

		Assertions.assertNotEquals(pointA, pointB);
	}

	@Test
	void testEqualsWithSuper() {
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);

		Assertions.assertEquals(pointA, pointB);
	}

	@Test
	void testNotEqualsWithSuper() {
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 200);
		var pointC = new ObjectMethodGenerationTestUtils.Point3DWithSuper(200, 200, 200);

		//Test all combinations
		Assertions.assertNotEquals(pointA, pointB);
		Assertions.assertNotEquals(pointB, pointC);
		Assertions.assertNotEquals(pointC, pointA);
	}

	@Test
	void testHashCodeWithTransientIgnored() {
		var pointA = new ObjectMethodGenerationTestUtils.Point2D(100, 100);
		pointA.testTransient = 10000L;
		var pointB = new ObjectMethodGenerationTestUtils.Point2D(100, 100);
		pointB.testTransient = -10000L;

		Assertions.assertEquals(pointA.hashCode(), pointB.hashCode());
	}

	@Test
	void testHashCodeEqualsWithoutSuper() {
		//Only the z field (last parameter) is considered
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 100, 200);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 200, 200);
		var pointC = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(200, 100, 200);

		//Test all combinations
		Assertions.assertEquals(pointA.hashCode(), pointB.hashCode());
		Assertions.assertEquals(pointB.hashCode(), pointC.hashCode());
		Assertions.assertEquals(pointC.hashCode(), pointA.hashCode());
	}

	@Test
	void testHashCodeNotEqualsWithoutSuper() {
		//Only the z field (last parameter) is considered
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 100, 100);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(200, 200, 200);

		Assertions.assertNotEquals(pointA.hashCode(), pointB.hashCode());
	}

	@Test
	void testHashCodeEqualsWithSuper() {
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);

		Assertions.assertEquals(pointA.hashCode(), pointB.hashCode());
	}

	@Test
	void testHashCodeNotEqualsWithSuper() {
		var pointA = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 100);
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithSuper(100, 100, 200);
		var pointC = new ObjectMethodGenerationTestUtils.Point3DWithSuper(200, 200, 200);

		//Test all combinations
		Assertions.assertNotEquals(pointA.hashCode(), pointB.hashCode());
		Assertions.assertNotEquals(pointB.hashCode(), pointC.hashCode());
		Assertions.assertNotEquals(pointC.hashCode(), pointA.hashCode());
	}

	@Test
	void testToString() {
		var pointA = new ObjectMethodGenerationTestUtils.Point2D(100, 200);
		pointA.testTransient = 10000L;
		var pointB = new ObjectMethodGenerationTestUtils.Point3DWithoutSuper(100, 100, 100);

		Assertions.assertEquals("Point2D[x=100, y=200]", pointA.toString());
		Assertions.assertEquals("Point3DWithoutSuper[z=100]", pointB.toString());
	}
}
