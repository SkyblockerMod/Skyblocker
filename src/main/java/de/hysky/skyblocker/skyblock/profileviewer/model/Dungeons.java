package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class Dungeons {
	@SerializedName("last_dungeon_run")
	public String lastDungeonRun;
	public int secrets;
	@SerializedName("selected_dungeon_class")
	public String selectedDungeonClass;
	@SerializedName("daily_runs")
	public DailyRuns dailyRuns = new DailyRuns();
	/**
	 * Croesus storage data
	 */
	public Treasures treasures = new Treasures();
	@SerializedName("player_classes")
	public Map<String, ClassStats> classStats = Map.of();
	@SerializedName("dungeon_types")
	public PerDungeonType dungeonInfo;

	public static class PerDungeonType {
		@SerializedName("master_catacombs")
		public GenericCatacombs masterModeCatacombs = new GenericCatacombs();
		public DefaultCatacombs catacombs = new DefaultCatacombs();
	}

	public static class ClassStats {
		public double experience;

		public LevelFinder.LevelInfo getLevelInfo() {
			return LevelFinder.getLevelInfo("Catacombs", (long) experience);
		}
	}

	public static class DailyRuns {
		/**
		 * This is days since UNIX epoch.
		 */
		@SerializedName("current_day_stamp")
		public int currentDayStamp;

		public @Nullable LocalDate getLastDailyRunDate() {
			if (currentDayStamp == 0)
				return null;
			return LocalDate.ofEpochDay(currentDayStamp);
		}

		@SerializedName("completed_runs_count")
		public int completedRunsCount;
	}
}
