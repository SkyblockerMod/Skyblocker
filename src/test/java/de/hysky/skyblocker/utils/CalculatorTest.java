package de.hysky.skyblocker.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculatorTest {
    @Test
    void testCalculator() {
        //check the magnitudes are working
        Assertions.assertEquals(Calculator.calculate("1k"), 1000);
        Assertions.assertEquals(Calculator.calculate("0.12k"), 120);
        Assertions.assertEquals(Calculator.calculate("1k + 0.12k"), 1120);
        Assertions.assertEquals(Calculator.calculate("1 + 1s + 1k + 1m + 1b"), 1001001065);

        //check precedence works
        Assertions.assertEquals(Calculator.calculate("5 + 2 * 2"), 9);
        Assertions.assertEquals(Calculator.calculate("5 - 2 / 2"), 4);
        Assertions.assertEquals(Calculator.calculate("5 * (1 + 2)"), 15);

        //check implicit multiplication
        Assertions.assertEquals(Calculator.calculate("5(2 + 2)"), 20);

        //check unclosed parenthesis work
        Assertions.assertEquals(Calculator.calculate("5(2 + 2"), 20);
    }
}
