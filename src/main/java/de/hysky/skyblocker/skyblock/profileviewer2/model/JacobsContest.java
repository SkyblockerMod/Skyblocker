package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacobsContest {
	public boolean talked;
	public boolean migration;

	public Perks perks = new Perks();

	public static class Perks {
		@SerializedName("farming_level_cap")
		public int farmingLevelCap;
		@SerializedName("double_drops")
		public int doubleDrops;
		@SerializedName("personal_bests")
		public boolean personalBests;
	}

	@SerializedName("medals_inv")
	public MedalsInventory medalsInventory = new MedalsInventory();

	public static class MedalsInventory {
		public int bronze;
		public int silver;
		public int gold;

		public int getTotalMedals() {
			return this.bronze + this.silver + this.gold;
		}
	}

	@SerializedName("personal_bests")
	public Map<String, Long> personalBests = new HashMap<>();


	@SerializedName("unique_brackets")
	public Map<String, List<String>> uniqueBrackets = new HashMap<>();

	/**
	 * Contest ID Format: Contest keys like "99:11_7:POTATO_ITEM" are "SKYBLOCK_YEAR:MONTH_DAY:CROP"
	 */
	public Map<String, Contest> contests = new HashMap<>();

	public static class Contest {
		@SerializedName("claimed_rewards")
		public boolean claimedRewards;
		@SerializedName("claimed_position")
		public int claimedPosition;
		@SerializedName("claimed_participants")
		public int claimedParticipants;
		public int collected;
	}
}
