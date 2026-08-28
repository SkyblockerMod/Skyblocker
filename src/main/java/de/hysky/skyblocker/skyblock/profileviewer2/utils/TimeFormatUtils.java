package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeFormatUtils {
	// The "central" world time zone (also known as UTC). This is the time zone used for dungeon
	// daily resets.
	public static final ZoneId GMT = ZoneId.of("GMT");

	public static String getShortestReasonableUnit(Duration duration) {
		long days = duration.toDays();
		long hours = duration.toHours();
		long minutes = duration.toMinutes();
		long seconds = duration.toSeconds();
		long millis = duration.toMillis();

		if (days > 0) {
			return days + "d";
		} else if (hours > 0) {
			return hours + "h";
		} else if (minutes > 0) {
			return minutes + "m";
		} else if (seconds > 0) {
			return seconds + "s";
		} else {
			return millis + "ms";
		}
	}

	public static String getDurationString(long millis) {
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

		return String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds);
	}
}
