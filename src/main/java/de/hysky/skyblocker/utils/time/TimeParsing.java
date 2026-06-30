package de.hysky.skyblocker.utils.time;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import de.hysky.skyblocker.utils.Formatters;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParsing {
	private static final Pattern DURATION_UNIT_PATTERN = Pattern.compile("(\\d+)(y|mo|d|h|m|s)");
	/**
	 * Accepts both {@link Formatters#SKYBLOCK_TIME_FORMATTER} and {@link DateTimeFormatter#ISO_INSTANT}
	 */
	public static final Codec<Instant> INSTANT_CODEC = Codec.STRING.flatXmap(
			s -> {
				try {
					TemporalAccessor parsed = Formatters.SKYBLOCK_TIME_FORMATTER.parse(s);
					return DataResult.success(SkyblockTime.SKYBLOCK_EPOCH
							.plus(parsed.getLong(SkyblockTimeField.HOUR_OF_DAY), SkyblockTimeField.HOUR_OF_DAY.getBaseUnit())
							.plus(parsed.getLong(SkyblockTimeField.DAY_OF_MONTH), SkyblockTimeField.DAY_OF_MONTH.getBaseUnit())
							.plus(parsed.getLong(SkyblockTimeField.MONTH_OF_YEAR), SkyblockTimeField.MONTH_OF_YEAR.getBaseUnit())
							.plus(parsed.getLong(SkyblockTimeField.YEAR), SkyblockTimeField.YEAR.getBaseUnit())
					);
				} catch (DateTimeParseException _) {}
				try {
					return DataResult.success(Instant.parse(s));
				} catch (DateTimeParseException _) {}
				return DataResult.error(() -> "Could not parse instant.");
			},
			_ -> DataResult.error(() -> "Encoding not supported.")
	);

	/**
	 * Format by MonkeysHK.
	 */
	public static final Codec<Duration> SKY_DURATION_CODEC = Codec.STRING.flatXmap(
			s -> {
				boolean isSkyblock = s.endsWith("-S");
				if (!isSkyblock && !s.endsWith("-U")) return DataResult.error(() -> "Unknown suffix, must be either -S or -U");
				Matcher matcher = DURATION_UNIT_PATTERN.matcher(s);
				Duration duration = Duration.ZERO;
				while (matcher.find()) {
					int amount = Integer.parseInt(matcher.group(1));
					TemporalUnit unit = switch (matcher.group(2)) {
						case "s" -> isSkyblock ? null : ChronoUnit.SECONDS;
						case "m" -> isSkyblock ? null : ChronoUnit.MINUTES;
						case "h" -> isSkyblock ? SkyblockTimeUnit.HOURS : ChronoUnit.HOURS;
						case "d" -> isSkyblock ? SkyblockTimeUnit.DAYS : ChronoUnit.DAYS;
						case "mo" -> isSkyblock ? SkyblockTimeUnit.MONTHS : ChronoUnit.MONTHS;
						case "y" -> isSkyblock ? SkyblockTimeUnit.YEARS : ChronoUnit.YEARS;
						default -> null;
					};
					if (unit == null) continue;
					duration = duration.plus(amount, unit);
				}
				return DataResult.success(duration);
			},
			_ -> DataResult.error(() -> "Encoding not supported.")
	);
}
