package de.hysky.skyblocker.utils;

import java.time.Instant;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FormattersTest {

	@BeforeAll
	public static void setupEnvironment() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@Test
	void testIntegerNumbers() {
		Assertions.assertEquals("100,000,000", Formatters.INTEGER_NUMBERS.format(100_000_000));
		Assertions.assertEquals("99,999,999", Formatters.INTEGER_NUMBERS.format(99_999_999.4));
		Assertions.assertEquals("88,888,888", Formatters.INTEGER_NUMBERS.format(88_888_888.5)); //Half even rounding
		Assertions.assertEquals("77,777,777", Formatters.INTEGER_NUMBERS.format(77_777_776.7));
	}

	@Test
	void testDoubleNumbers() {
		Assertions.assertEquals("100,000,000.15", Formatters.DOUBLE_NUMBERS.format(100_000_000.152341));
		Assertions.assertEquals("99,999,999.98", Formatters.DOUBLE_NUMBERS.format(99_999_999.978));
	}

	@Test
	void testFloatNumbers() {
		Assertions.assertEquals("100,000,000.8", Formatters.FLOAT_NUMBERS.format(100_000_000.7834));
		Assertions.assertEquals("99,999,999.8", Formatters.FLOAT_NUMBERS.format(99_999_999.84243));
	}

	@Test
	void testShortIntegerNumbers() {
		Assertions.assertEquals("16B", Formatters.SHORT_INTEGER_NUMBERS.format(15_500_000_000L));
		Assertions.assertEquals("10M", Formatters.SHORT_INTEGER_NUMBERS.format(10_200_000));
		Assertions.assertEquals("5K", Formatters.SHORT_INTEGER_NUMBERS.format(5_000));
	}

	@Test
	void testShortFloatNumbers() {
		Assertions.assertEquals("14.5B", Formatters.SHORT_FLOAT_NUMBERS.format(14_500_000_000L));
		Assertions.assertEquals("8.3M", Formatters.SHORT_FLOAT_NUMBERS.format(8_300_000));
		Assertions.assertEquals("24.7K", Formatters.SHORT_FLOAT_NUMBERS.format(24_740));
	}

	@Test
	void testParseNumbers() {
		Assertions.assertInstanceOf(Long.class, Formatters.parseNumber("1,024"));
		Assertions.assertEquals(1024, Formatters.parseNumber("1,024").intValue());
		Assertions.assertEquals(123_456.789, Formatters.parseNumber("123,456.789"));
	}

	@Test
	void testDates() {
		long Thu_Jan_30th_2025_at_4_10_00_PM = 1738253400000L;
		long Fri_Jan_31st_2025_at_11_11_00_AM = 1738321860000L;
		long Sat_Feb_1st_2025_at_12_00_01_AM = 1738368001000L;

		Assertions.assertEquals("Thu Jan 30 2025 4:10:00 PM", Formatters.DATE_FORMATTER.format(Instant.ofEpochMilli(Thu_Jan_30th_2025_at_4_10_00_PM)));
		Assertions.assertEquals("Fri Jan 31 2025 11:11:00 AM", Formatters.DATE_FORMATTER.format(Instant.ofEpochMilli(Fri_Jan_31st_2025_at_11_11_00_AM)));
		Assertions.assertEquals("Sat Feb 1 2025 12:00:01 AM", Formatters.DATE_FORMATTER.format(Instant.ofEpochMilli(Sat_Feb_1st_2025_at_12_00_01_AM)));
	}
}
