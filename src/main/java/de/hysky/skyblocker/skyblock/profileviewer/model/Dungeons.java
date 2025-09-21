package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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

	public ClassStats getClassData(Class dungeonClass) {
		return classStats.getOrDefault(dungeonClass.getName().toLowerCase(), new ClassStats());
	}

	public enum Class {
		HEALER("Healer", Ico.S_POTION),
		MAGE("Mage", Ico.B_ROD),
		BERSERK("Berserk", Ico.IRON_SWORD),
		ARCHER("Archer", Ico.BOW),
		TANK("Tank", Ico.CHESTPLATE);

		private final String name;
		private final ItemStack itemStack;

		Class(String name, ItemStack itemStack) {
			this.name = name;
			this.itemStack = itemStack;
		}

		public String getName() { return name; }

		public ItemStack getIcon() { return itemStack; }
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
