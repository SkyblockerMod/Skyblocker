package de.hysky.skyblocker.utils;

import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CalculatorTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testSeparators() {
		assertCalculation(11254123, "1_125_41,23");
		assertCalculation(11254123.341, "1_125_41,23_.341");
		assertCalculation(11254123., "1_125_41,23.");
		// Trailing decimal supported, leading decimal not supported. Tempting, but KISS
		assertThrows(".52");
	}

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
		assertCalculation(48, "3*4^2");
		assertCalculation(35, "3^3+(2^3)");
	}

	@Test
	void testInvalids() {
		assertThrows("5+asdf");
		assertThrows("5++3");
		assertThrows("2^");
		assertThrows("^");
		assertThrows("9 + 3* (0) )");
	}

	@Test
	void testFunctions() {
		assertCalculation(2, "sqrt(4)");
		assertCalculation(2, "log(100)");
		assertCalculation(4, "lg(16)");
		assertCalculation(3, "ln(20.0855369231876677409285296545817178969879078385541501443789342296988458780)");
		assertCalculation(120, "factorial(5)");
		assertCalculation(0, "sin(0)");
		assertCalculation(1, "cos(0)");
		assertCalculation(0, "tan(0)");
		assertCalculation(1, "abs(1)");
		assertCalculation(1, "floor(1.9)");
		assertCalculation(2, "ceil(1.1)");
		assertCalculation(3, "round(2.5)");
	}

	@Test
	void testCalculations() {
		assertCalculation(30, "5(2*sqrt(36)+3log(1))/2");
		assertCalculation(262144, "4^3^2");
		assertCalculation(3.0001220703125, "3+4*2/(1-5)^2^3");
		assertCalculation(-23635.272727272727, "(5 + 2 * sqrt(36) - 3 * lg(2))^2 - (4 * (0-sqrt(16))^2^3) / (3 + 256 / 2 * lg(65536) / 2^2^3)");
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
		Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(expected, Calculator.calculate(input)));
	}

	private void assertThrows(String input) {
		Assertions.assertThrows(Calculator.CalculatorException.class, () -> Calculator.calculate(input));
	}
}
