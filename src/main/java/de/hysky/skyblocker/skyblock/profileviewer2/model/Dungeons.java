package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.TimeFormatUtils;

public class Dungeons {
	@SerializedName("last_dungeon_run")
	public String lastDungeonRun = "";
	public int secrets;
	@SerializedName("selected_dungeon_class")
	public String selectedDungeonClass = "";
	@SerializedName("daily_runs")
	public DailyRuns dailyRuns = new DailyRuns();
	/// Croesus storage data
	public Treasures treasures = new Treasures();
	@SerializedName("player_classes")
	public Map<String, ClassStats> classStats = Map.of();

	public ClassStats getClassData(DungeonClass dungeonClass) {
		return classStats.getOrDefault(dungeonClass.apiName(), new ClassStats());
	}

	public LevelInfo getClassAverage(LoadingInformation info) {
		long cumulativeXp = 0;

		cumulativeXp += getClassData(DungeonClass.HEALER).getLevelInfo(info).xp();
		cumulativeXp += getClassData(DungeonClass.MAGE).getLevelInfo(info).xp();
		cumulativeXp += getClassData(DungeonClass.BERSERK).getLevelInfo(info).xp();
		cumulativeXp += getClassData(DungeonClass.ARCHER).getLevelInfo(info).xp();
		cumulativeXp += getClassData(DungeonClass.TANK).getLevelInfo(info).xp();

		long averageXp = cumulativeXp / 5;

		return LevelCalculator.getSkillLevel(averageXp, Skill.CATACOMBS, info);
	}

	public int getTotalDungeonRuns() {
		int normalRuns = (int) this.dungeonTypes.catacombs.tierCompletions.getManuallyCalculatedTotal();
		int masterRuns = (int) this.dungeonTypes.masterModeCatacombs.tierCompletions.getManuallyCalculatedTotal();

		return normalRuns + masterRuns;
	}

	public double getSecretsPerRun() {
		// Convert to double so we get the decimal places
		double runs = this.getTotalDungeonRuns();
		double secrets = this.secrets;

		return runs > 0 ? (double) secrets / runs : 0;
	}

	@SerializedName("dungeon_types")
	public PerDungeonType dungeonTypes = new PerDungeonType();

	public static class PerDungeonType {
		@SerializedName("master_catacombs")
		public GenericCatacombs masterModeCatacombs = new GenericCatacombs();
		public DefaultCatacombs catacombs = new DefaultCatacombs();
	}

	public static class ClassStats {
		public double experience;

		public LevelInfo getLevelInfo(LoadingInformation info) {
			return LevelCalculator.getSkillLevel((long) this.experience, Skill.CATACOMBS, info);
		}
	}

	public static class DailyRuns {
		public static final int MAX_DAILIES = 5;

		/// This is days since UNIX epoch.
		@SerializedName("current_day_stamp")
		public int currentDayStamp;
		@SerializedName("completed_runs_count")
		public int completedRunsCount;

		private @Nullable LocalDate getLastDailyRunDate() {
			if (this.currentDayStamp == 0) {
				return null;
			} else {
				return LocalDate.ofEpochDay(this.currentDayStamp);
			}
		}

		public int getDailyRunsCompleted() {
			LocalDate lastDay = this.getLastDailyRunDate();
			LocalDate currentDay = LocalDate.now(TimeFormatUtils.GMT);

			// Cap the run amount since we don't need to know the value beyond 5
			return currentDay.equals(lastDay) ? Math.min(this.completedRunsCount, MAX_DAILIES) : 0;
		}

		public Duration timeUntilReset() {
			Instant currentTime = Instant.now(Clock.system(TimeFormatUtils.GMT));
			Instant nextResetTime = LocalDate.now(TimeFormatUtils.GMT)
					.plusDays(1L)
					.atStartOfDay(TimeFormatUtils.GMT)
					.toInstant();

			return Duration.between(currentTime, nextResetTime);
		}
	}
}
