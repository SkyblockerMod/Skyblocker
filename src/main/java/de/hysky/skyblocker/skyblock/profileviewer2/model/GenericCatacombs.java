package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public class GenericCatacombs {
	@SerializedName("best_score")
	public PersonalBest bestScore = new PersonalBest();
	@SerializedName("fastest_time")
	public PersonalBest fastestTime = new PersonalBest();
	@SerializedName("mobs_killed")
	public AggregateStat mobsKilled = new AggregateStat();
	@SerializedName("most_mobs_killed")
	public PersonalBest mobsKilledInOneRun = new PersonalBest();
	/**
	 * Is this only for healer role or in general?
	 */
	@SerializedName("most_healing")
	public PersonalBest mostHealing = new PersonalBest();

	@SerializedName("tier_completions")
	public AggregateStat tierCompletions = new AggregateStat();
	/**
	 * Is this adding the milestones achieved across runs? Definitely a weird metric to track, and then not include any recent update in the API.
	 */
	@SerializedName("milestone_completions")
	public AggregateStat milestoneCompletions = new AggregateStat();
	@SerializedName("fastest_time_s")
	public PersonalBest fastestTimeS = new PersonalBest();
	@SerializedName("fastest_time_s_plus")
	public PersonalBest fastestTimeSPlus = new PersonalBest();
	@SerializedName("most_damage_mage")
	public PersonalBest mostMageDamage = new PersonalBest();
	@SerializedName("most_damage_archer")
	public PersonalBest mostDamageArcher = new PersonalBest();
	@SerializedName("most_damage_berserk")
	public PersonalBest mostDamageBeserk = new PersonalBest();
	@SerializedName("most_damage_tank")
	public PersonalBest mostDamageTank = new PersonalBest();
	@SerializedName("most_damage_healer")
	public PersonalBest mostDamageHealer = new PersonalBest();

	@SerializedName("highest_tier_completed")
	public int highestTierCompleted;

	/**
	 * Mapping of 0 indexed floor to a list of "best runs". This might be the run in which one of the {@link PersonalBest personal bests} was achieved, but i am not entirely sure.
	 */
	@SerializedName("best_runs")
	public Map<String, List<BestRun>> bestRuns = Map.of();

	public static class BestRun {
		public long timestamp;
		@SerializedName("score_exploration")
		public int scoreExploration;
		@SerializedName("score_speed")
		public int scoreSpeed;
		@SerializedName("score_skill")
		public int scoreSkill;
		@SerializedName("score_bonus")
		public int scoreBonus;
		/**
		 * One of {@code tank}, {@code healer}, etc.
		 */
		@SerializedName("dungeon_class")
		public String dungeonClass;
		public List<UUID> teammates = List.of();
		@SerializedName("elapsed_time")
		public int elapsedTime;
		@SerializedName("damage_dealt")
		public double damageDealt;
		public int deaths;
		@SerializedName("mobs_killed")
		public int mobsKilled;
		@SerializedName("secrets_found")
		public int secretsFound;
		@SerializedName("damage_mitigated")
		public double damageMitigated;
		@SerializedName("ally_healing")
		public double allyHealing;
	}

	public static class PerFloorDisambiguation {
		@SerializedName("0")
		public @Nullable Double entrance;
		@SerializedName("1")
		public @Nullable Double one;
		@SerializedName("2")
		public @Nullable Double two;
		@SerializedName("3")
		public @Nullable Double three;
		@SerializedName("4")
		public @Nullable Double four;
		@SerializedName("5")
		public @Nullable Double five;
		@SerializedName("6")
		public @Nullable Double six;
		@SerializedName("7")
		public @Nullable Double seven;

		/**
		 * @see #getValue
		 */
		public double getValueOrZero(int oneIndexedFloor) {
			var value = getValue(oneIndexedFloor);
			if (value == null) return 0;
			return value;
		}

		/**
		 * @param oneIndexedFloor one indexed floor (F1 = 1), with Entrance = 0.
		 */
		public @Nullable Double getValue(int oneIndexedFloor) {
			return switch (oneIndexedFloor) {
				case 0 -> entrance;
				case 1 -> one;
				case 2 -> two;
				case 3 -> three;
				case 4 -> four;
				case 5 -> five;
				case 6 -> six;
				case 7 -> seven;

				default -> throw new IllegalStateException("Unexpected floor: " + oneIndexedFloor);
			};
		}
	}

	public static class PersonalBest extends PerFloorDisambiguation {
		public @Nullable Double best;
	}

	public static class AggregateStat extends PerFloorDisambiguation {
		/**
		 * @see #getManuallyCalculatedTotal()
		 */
		public double total;

		private static double coerce0(@Nullable Double d) {
			return d != null ? d : 0;
		}

		/**
		 * {@link #total} seems to be off by quite a bit sometimes. This manually calculates the total.
		 */
		public double getManuallyCalculatedTotal() {
			double result = 0;
			result += coerce0(entrance);
			result += coerce0(one);
			result += coerce0(two);
			result += coerce0(three);
			result += coerce0(four);
			result += coerce0(five);
			result += coerce0(six);
			result += coerce0(seven);
			return result;
		}

	}
}
