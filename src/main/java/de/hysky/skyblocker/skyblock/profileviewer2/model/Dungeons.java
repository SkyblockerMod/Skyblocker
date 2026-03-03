package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public class Dungeons {
	@SerializedName("last_dungeon_run")
	public String lastDungeonRun = "";
	public int secrets;
	@SerializedName("selected_dungeon_class")
	public String selectedDungeonClass = "";
	@SerializedName("daily_runs")
	public DailyRuns dailyRuns = new DailyRuns();
	/**
	 * Croesus storage data
	 */
	public Treasures treasures = new Treasures();
	@SerializedName("player_classes")
	public Map<String, ClassStats> classStats = Map.of();

	public ClassStats getClassData(DungeonClass dungeonClass) {
		return classStats.getOrDefault(dungeonClass.name().toLowerCase(Locale.ENGLISH), new ClassStats());
	}

	@SerializedName("dungeon_types")
	public PerDungeonType dungeonInfo;

	public static class PerDungeonType {
		@SerializedName("master_catacombs")
		public GenericCatacombs masterModeCatacombs = new GenericCatacombs();
		public DefaultCatacombs catacombs = new DefaultCatacombs();
	}

	public static class ClassStats {
		public double experience;

		public LevelInfo getLevelInfo() {
			return LevelCalculator.getSkillLevel((long) this.experience, Skill.CATACOMBS);
		}
	}

	public static class DailyRuns {
		/**
		 * This is days since UNIX epoch.
		 */
		@SerializedName("current_day_stamp")
		public int currentDayStamp;

		public @Nullable LocalDate getLastDailyRunDate() {
			if (this.currentDayStamp == 0) {
				return null;
			} else {
				return LocalDate.ofEpochDay(this.currentDayStamp);
			}
		}

		@SerializedName("completed_runs_count")
		public int completedRunsCount;
	}
}
