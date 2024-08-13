package de.hysky.skyblocker.utils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
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
	public static final AtomicInteger skyblockHour = new AtomicInteger(0);
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Time");

	//All time lengths are in milliseconds
	public static final double HOUR_LENGTH = 50000;
	public static final double DAY_LENGTH = HOUR_LENGTH * 24;
	public static final double MONTH_LENGTH = DAY_LENGTH * 31;
	public static final double SEASON_LENGTH = MONTH_LENGTH * 3;
	public static final double YEAR_LENGTH = SEASON_LENGTH * 4;

	public static final Event<OnHourChange> HOUR_CHANGE = EventFactory.createArrayBacked(OnHourChange.class, listeners -> hour -> {
		for (OnHourChange listener : listeners) {
			listener.onHourChange(hour);
		}
	});
	public static final Event<OnDayChange> DAY_CHANGE = EventFactory.createArrayBacked(OnDayChange.class, listeners -> day -> {
		for (OnDayChange listener : listeners) {
			listener.onDayChange(day);
		}
	});
	public static final Event<OnMonthChange> MONTH_CHANGE = EventFactory.createArrayBacked(OnMonthChange.class, listeners -> month -> {
		for (OnMonthChange listener : listeners) {
			listener.onMonthChange(month);
		}
	});
	public static final Event<OnSeasonChange> SEASON_CHANGE = EventFactory.createArrayBacked(OnSeasonChange.class, listeners -> season -> {
		for (OnSeasonChange listener : listeners) {
			listener.onSeasonChange(season);
		}
	});
	public static final Event<OnYearChange> YEAR_CHANGE = EventFactory.createArrayBacked(OnYearChange.class, listeners -> year -> {
		for (OnYearChange listener : listeners) {
			listener.onYearChange(year);
		}
	});
	public static final Event<OnTimeUpdate> TIME_UPDATE = EventFactory.createArrayBacked(OnTimeUpdate.class, listeners -> (year, season, month, day, hour) -> {
		for (OnTimeUpdate listener : listeners) {
			listener.onTimeUpdate(year, season, month, day, hour);
		}
	});

	private SkyblockTime() {
	}

	/**
	 * Updates the time and schedules a cyclic scheduler which will update the time every hour, to be run on the next hour change.
	 */
	@Init
	public static void init() {
		updateTime();
		//scheduleCyclic already runs the task upon scheduling, so there's no need to call updateTime() in the lambda as well
		//The division by 50 is to convert the time to ticks
		Scheduler.INSTANCE.schedule(() -> Scheduler.INSTANCE.scheduleCyclic(SkyblockTime::updateTime, (int) (HOUR_LENGTH / 50)), (int) (HOUR_LENGTH - (getSkyblockMillis() % HOUR_LENGTH)) / 50);
	}

	public static long getSkyblockMillis() {
		return System.currentTimeMillis() - SKYBLOCK_EPOCH;
	}

	private static int calculateSkyblockYear() {
		return (int) (Math.floor(getSkyblockMillis() / YEAR_LENGTH) + 1);
	}

	private static int calculateSkyblockMonth() {
		return (int) (Math.floor(getSkyblockMillis() / MONTH_LENGTH) % 12);
	}

	private static int calculateSkyblockDay() {
		return (int) (Math.floor(getSkyblockMillis() / DAY_LENGTH) % 31 + 1);
	}

	private static int calculateSkyblockHour() {
		return (int) (Math.floor(getSkyblockMillis() / HOUR_LENGTH) % 24);
	}

	//This could probably be compacted by abstracting the logic into a method that takes a Supplier and a Consumer, etc. but there's really no need
	private static void updateTime() {
		int year = calculateSkyblockYear();
		if (skyblockYear.get() != year) {
			skyblockYear.set(year);
			YEAR_CHANGE.invoker().onYearChange(year);
		}
		Season season = Season.values()[calculateSkyblockMonth() / 3];
		if (skyblockSeason.get() != season) {
			skyblockSeason.set(season);
			SEASON_CHANGE.invoker().onSeasonChange(season);
		}
		Month month = Month.values()[calculateSkyblockMonth()];
		if (skyblockMonth.get() != month) {
			skyblockMonth.set(month);
			MONTH_CHANGE.invoker().onMonthChange(month);
		}
		int day = calculateSkyblockDay();
		if (skyblockDay.get() != day) {
			skyblockDay.set(day);
			DAY_CHANGE.invoker().onDayChange(day);
		}
		int hour = calculateSkyblockHour();
		if (skyblockHour.get() != hour) {
			skyblockHour.set(hour);
			HOUR_CHANGE.invoker().onHourChange(hour);
		}
		TIME_UPDATE.invoker().onTimeUpdate(year, season, month, day, hour);
		LOGGER.info("[Skyblocker Time] Skyblock time updated to Year {}, Season {}, Month {}, Day {}, Hour {}", year, season, month, day, hour);
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

	public interface OnHourChange {
		void onHourChange(int hour);
	}

	public interface OnDayChange {
		void onDayChange(int day);
	}

	public interface OnMonthChange {
		void onMonthChange(Month month);
	}

	public interface OnSeasonChange {
		void onSeasonChange(Season season);
	}

	public interface OnYearChange {
		void onYearChange(int year);
	}

	public interface OnTimeUpdate {
		void onTimeUpdate(int year, Season season, Month month, int day, int hour);
	}
}
