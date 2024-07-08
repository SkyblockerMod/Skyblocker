package de.hysky.skyblocker.utils;

import de.hysky.skyblocker.utils.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SkyblockTime {
	private static final long SKYBLOCK_EPOCH = 1560275700000L;
	public static final AtomicInteger skyblockYear = new AtomicInteger(0);
	public static final AtomicReference<Season> skyblockSeason = new AtomicReference<>(Season.SPRING);
	public static final AtomicReference<Month> skyblockMonth = new AtomicReference<>(Month.EARLY_SPRING);
	public static final AtomicInteger skyblockDay = new AtomicInteger(0);
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Time");

	private SkyblockTime() {
	}

	public static void init() {
		updateTime();
		//ScheduleCyclic already runs the task upon scheduling, so there's no need to call updateTime() here
		Scheduler.INSTANCE.schedule(() -> Scheduler.INSTANCE.scheduleCyclic(SkyblockTime::updateTime, 1200 * 20), (int) (1200000 - (getSkyblockMillis() % 1200000)) / 50);
	}

	public static long getSkyblockMillis() {
		return System.currentTimeMillis() - SKYBLOCK_EPOCH;
	}

	private static int getSkyblockYear() {
		return (int) (Math.floor(getSkyblockMillis() / 446400000.0) + 1);
	}

	private static int getSkyblockMonth() {
		return (int) (Math.floor(getSkyblockMillis() / 37200000.0) % 12);
	}

	private static int getSkyblockDay() {
		return (int) (Math.floor(getSkyblockMillis() / 1200000.0) % 31 + 1);
	}

	private static void updateTime() {
		skyblockYear.set(getSkyblockYear());
		skyblockSeason.set(Season.values()[getSkyblockMonth() / 3]);
		skyblockMonth.set(Month.values()[getSkyblockMonth()]);
		skyblockDay.set(getSkyblockDay());
		LOGGER.info("[Skyblocker Time] Skyblock time updated to Year {}, Season {}, Month {}, Day {}", skyblockYear.get(), skyblockSeason.get(), skyblockMonth.get(), skyblockDay.get());
	}

	public enum Season {
		SPRING, SUMMER, FALL, WINTER
	}

	public enum Month {
		EARLY_SPRING, SPRING, LATE_SPRING,
		EARLY_SUMMER, SUMMER, LATE_SUMMER,
		EARLY_FALL, FALL, LATE_FALL,
		EARLY_WINTER, WINTER, LATE_WINTER
	}
}
