package de.hysky.skyblocker.utils.time;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;

public enum SkyblockTimeField implements TemporalField {
	HOUR_OF_DAY(SkyblockTimeUnit.HOURS, SkyblockTimeUnit.DAYS, ValueRange.of(0, 23)),
	DAY_OF_MONTH(SkyblockTimeUnit.DAYS, SkyblockTimeUnit.MONTHS, ValueRange.of(1, 31)),
	DAY_OF_YEAR(SkyblockTimeUnit.DAYS, SkyblockTimeUnit.YEARS, ValueRange.of(1, 372)),
	MONTH_OF_YEAR(SkyblockTimeUnit.MONTHS, SkyblockTimeUnit.YEARS, ValueRange.of(1, 12)),
	SEASON_OF_YEAR(SkyblockTimeUnit.SEASONS, SkyblockTimeUnit.YEARS, ValueRange.of(1, 4)),
	YEAR(SkyblockTimeUnit.YEARS, ChronoUnit.FOREVER, ValueRange.of(1, Long.MAX_VALUE));

	private final TemporalUnit baseUnit;
	private final TemporalUnit rangeUnit;
	private final ValueRange range;

	SkyblockTimeField(TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range) {
		this.baseUnit = baseUnit;
		this.rangeUnit = rangeUnit;
		this.range = range;
	}

	@Override
	public TemporalUnit getBaseUnit() {
		return baseUnit;
	}

	@Override
	public TemporalUnit getRangeUnit() {
		return rangeUnit;
	}

	@Override
	public ValueRange range() {
		return range;
	}

	@Override
	public boolean isDateBased() {
		return false;
	}

	@Override
	public boolean isTimeBased() {
		return false;
	}

	@Override
	public boolean isSupportedBy(TemporalAccessor temporal) {
		// epoch day will have EXTREME rounding issues
		return temporal.isSupported(ChronoField.INSTANT_SECONDS) || temporal.isSupported(ChronoField.EPOCH_DAY);
	}

	@Override
	public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
		return range;
	}

	@Override
	public long getFrom(TemporalAccessor temporal) {
		long seconds;
		if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
			seconds = temporal.getLong(ChronoField.INSTANT_SECONDS);
		} else if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
			seconds = temporal.getLong(ChronoField.EPOCH_DAY) * ChronoUnit.DAYS.getDuration().getSeconds();
		} else throw new UnsupportedTemporalTypeException("Temporal must support epoch seconds or days.");

		seconds -= SkyblockTime.SKYBLOCK_EPOCH.getEpochSecond();
		long value;
		if (seconds < 0) value = 0; // probably not worth throwing a tantrum
		else {
			seconds %= rangeUnit.getDuration().toSeconds();
			value = seconds / baseUnit.getDuration().getSeconds();
		}
		if (this != HOUR_OF_DAY) value++; // Only one that starts at 0
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Temporal> R adjustInto(R temporal, long newValue) {
		range.checkValidValue(newValue, this);
		long currentValue = temporal.getLong(this);
		long offset = newValue - currentValue;
		return (R) temporal.plus(offset, baseUnit);
	}
}
