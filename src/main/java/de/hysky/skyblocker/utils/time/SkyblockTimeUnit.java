package de.hysky.skyblocker.utils.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

public enum SkyblockTimeUnit implements TemporalUnit {
	HOURS(Duration.ofSeconds(50)),
	DAYS(Duration.ofMinutes(20)),
	MONTHS(Duration.ofMinutes(10 * 60 + 20)),
	SEASONS(Duration.ofHours(31)),
	YEARS(Duration.ofHours(124)),
	CENTURIES(Duration.ofHours(516 * 24 + 16));

	private final Duration duration;

	SkyblockTimeUnit(Duration duration) {
		this.duration = duration;
	}

	@Override
	public Duration getDuration() {
		return duration;
	}

	@Override
	public boolean isDurationEstimated() {
		return false;
	}

	@Override
	public boolean isDateBased() {
		return false;
	}

	@Override
	public boolean isTimeBased() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Temporal> R addTo(R temporal, long amount) {
		if (temporal.isSupported(ChronoUnit.SECONDS)) {
			return (R) temporal.plus(duration.toSeconds() * amount, ChronoUnit.SECONDS);
		} else if (temporal.isSupported(ChronoUnit.DAYS)) {
			return (R) temporal.plus(duration.toDays() * amount, ChronoUnit.DAYS);
		}
		throw new UnsupportedTemporalTypeException("Temporal must support seconds or days.");
	}

	@Override
	public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
		if (temporal1Inclusive.isSupported(ChronoUnit.SECONDS)) {
			long seconds = temporal1Inclusive.until(temporal2Exclusive, ChronoUnit.SECONDS);
			return seconds / duration.toSeconds();
		} else if (temporal1Inclusive.isSupported(ChronoUnit.DAYS)) {
			long days = temporal1Inclusive.until(temporal2Exclusive, ChronoUnit.DAYS);
			return days * ChronoUnit.DAYS.getDuration().toSeconds() / duration.toSeconds();
		}
		throw new UnsupportedTemporalTypeException("Temporal must support seconds or days.");
	}
}
