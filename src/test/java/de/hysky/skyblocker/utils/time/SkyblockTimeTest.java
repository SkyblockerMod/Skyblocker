package de.hysky.skyblocker.utils.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class SkyblockTimeTest {
	private static final long TEST_EPOCH_SECONDS = 1779575826; // 6th of early summer 492

	@BeforeAll
	public static void setupEnvironment() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@Test
	public void testAddSkyblockDays() {
		LocalDateTime start = LocalDateTime.of(2025, 10, 24, 11, 24);
		LocalDateTime expected = LocalDateTime.of(2025, 10, 24, 12, 44); // + 4 skyblock days
		Assertions.assertEquals(expected, start.plus(4, SkyblockTimeUnit.DAYS));
	}

	@Test
	public void testAddSkyblockMonths() {
		LocalDateTime start = LocalDateTime.of(2025, 10, 24, 11, 24);
		LocalDateTime expected = LocalDateTime.of(2025, 10, 25, 8, 4); // + 2 skyblock months, +20 hours and 40 minutes
		Assertions.assertEquals(expected, start.plus(2, SkyblockTimeUnit.MONTHS));
	}

	@Test
	public void testAddSkyblockYear() {
		LocalDateTime start = LocalDateTime.of(2025, 10, 24, 11, 24);
		LocalDateTime expected = LocalDateTime.of(2025, 10, 29, 15, 24); // + 1 skyblock year, +5 days and 4 hours
		Assertions.assertEquals(expected, start.plus(1, SkyblockTimeUnit.YEARS));
	}

	@Test
	public void testQueryDayOfMonth() {
		Instant start = Instant.ofEpochSecond(TEST_EPOCH_SECONDS);
		Assertions.assertEquals(6, start.getLong(SkyblockTimeField.DAY_OF_MONTH));
	}

	@Test
	public void testQueryMonthOfYear() {
		Instant start = Instant.ofEpochSecond(TEST_EPOCH_SECONDS);
		Assertions.assertEquals(4, start.getLong(SkyblockTimeField.MONTH_OF_YEAR));
		Assertions.assertEquals(SkyblockTime.Month.EARLY_SUMMER, start.query(SkyblockQueries.MONTH));
	}

	@Test
	public void testQueryYear() {
		Instant start = Instant.ofEpochSecond(TEST_EPOCH_SECONDS);
		Assertions.assertEquals(492, start.getLong(SkyblockTimeField.YEAR));
	}
}
