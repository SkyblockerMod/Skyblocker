package de.hysky.skyblocker.utils;

import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculatorTest {
	@Test
	void testShorthands() {
		assertCalculation(1000, "1k");
		assertCalculation(120, "0.12k");
		assertCalculation(1120, "1k + 0.12k");
		assertCalculation(1001001065, "1 + 1s + 1k + 1m + 1b");
	}

	@Test
	void testPrecedence() {
		assertCalculation(9, "5 + 2 * 2");
		assertCalculation(4, "5 - 2 / 2");
		assertCalculation(15, "5 * (1 + 2)");
	}

	@Test
	void testImplicitMultiplication() {
		assertCalculation(20, "5(2 + 2)");
	}

	@Test
	void testImplicitClosingParenthesis() {
		assertCalculation(20, "5(2 + 2");
	}

	@Test
	void testFloatingPointError() {
		SignCalculator.calculate("262.6m");
		Assertions.assertEquals("2.626E8", SignCalculator.getNewValue(true));
	}

	private void assertCalculation(double expected, String input) {
		Assertions.assertEquals(expected, Calculator.calculate(input));
	}
}
