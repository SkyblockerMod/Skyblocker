package de.hysky.skyblocker.skyblock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.regex.Matcher;

import static de.hysky.skyblocker.utils.Formatters.SHORT_FLOAT_NUMBERS;

public class HealthBarsTest {

	@Test
	void testHealthPatternNoAbbreviation() throws ParseException {
		Matcher match = HealthBars.HEALTH_PATTERN.matcher("Mob Name 99/100❤");
		match.find();
		Assertions.assertEquals(99L, SHORT_FLOAT_NUMBERS.parse(match.group(1)));
		Assertions.assertEquals(100L, SHORT_FLOAT_NUMBERS.parse(match.group(4)));
	}

	@Test
	void testHealthPatternAbbreviation() throws ParseException {
		Matcher match = HealthBars.HEALTH_PATTERN.matcher("Mob Name 100K/3.1M❤");
		match.find();
		Assertions.assertEquals(100_000L, SHORT_FLOAT_NUMBERS.parse(match.group(1)));
		Assertions.assertEquals(3_100_000L, SHORT_FLOAT_NUMBERS.parse(match.group(4)));
	}

	@Test
	void testHelthOnlyPatternNoAbbreviation() throws ParseException {
		Matcher match = HealthBars.HEALTH_ONLY_PATTERN.matcher("Mob Name 500❤");
		match.find();
		Assertions.assertEquals(500L, SHORT_FLOAT_NUMBERS.parse(match.group(1)));
	}

	@Test
	void testHealthOnlyPatternAbbreviation() throws ParseException {
		Matcher match = HealthBars.HEALTH_ONLY_PATTERN.matcher("Mob Name 58M❤");
		match.find();
		Assertions.assertEquals(58_000_000L, SHORT_FLOAT_NUMBERS.parse(match.group(1)));
	}
}
