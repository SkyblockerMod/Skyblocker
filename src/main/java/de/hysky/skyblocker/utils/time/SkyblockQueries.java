package de.hysky.skyblocker.utils.time;

import java.time.temporal.TemporalQuery;

import org.jspecify.annotations.Nullable;

public final class SkyblockQueries {
	private SkyblockQueries() {}

	public static final TemporalQuery<SkyblockTime.@Nullable Month> MONTH = temporal -> temporal.isSupported(SkyblockTimeField.MONTH_OF_YEAR) ? SkyblockTime.Month.values()[temporal.get(SkyblockTimeField.MONTH_OF_YEAR) - 1] : null;
	public static final TemporalQuery<SkyblockTime.@Nullable Season> SEASON = temporal -> temporal.isSupported(SkyblockTimeField.SEASON_OF_YEAR) ? SkyblockTime.Season.values()[temporal.get(SkyblockTimeField.SEASON_OF_YEAR) - 1] : null;
}
