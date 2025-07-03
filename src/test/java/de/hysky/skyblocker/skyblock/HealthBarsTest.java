package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.utils.Formatters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

public class HealthBarsTest {

	@Test
	void testHealthPatternNoAbbreviation() {
		Matcher match = HealthBars.HEALTH_PATTERN.matcher("Mob Name 99/100❤");
		Assertions.assertTrue(match.find());
		Assertions.assertEquals(99L, Formatters.parseNumber(match.group(1)));
		Assertions.assertEquals(100L, Formatters.parseNumber(match.group(4)));
	}

	@Test
	void testHealthPatternAbbreviation() {
		Matcher match = HealthBars.HEALTH_PATTERN.matcher("Mob Name 100K/3.1M❤");
		Assertions.assertTrue(match.find());
		Assertions.assertEquals(100_000L, Formatters.parseNumber(match.group(1)));
		Assertions.assertEquals(3_100_000L, Formatters.parseNumber(match.group(4)));
	}

	@Test
	void testHealthOnlyPatternNoAbbreviation() {
		Matcher match = HealthBars.HEALTH_ONLY_PATTERN.matcher("Mob Name 500❤");
		Assertions.assertTrue(match.find());
		Assertions.assertEquals(500L, Formatters.parseNumber(match.group(1)));
	}

	@Test
	void testHealthOnlyPatternAbbreviation() {
		Matcher match = HealthBars.HEALTH_ONLY_PATTERN.matcher("Mob Name 58M❤");
		Assertions.assertTrue(match.find());
		Assertions.assertEquals(58_000_000L, Formatters.parseNumber(match.group(1)));
	}
}
