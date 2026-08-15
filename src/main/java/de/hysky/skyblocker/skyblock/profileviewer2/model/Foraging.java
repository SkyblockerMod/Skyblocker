package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Foraging {
	@SerializedName("fish_family")
	public List<String> fishFamily = List.of();
	public Starlyn starlyn = new Starlyn();
	public Hina hina = new Hina();
	@SerializedName("tree_gifts")
	public TreeGifts treeGifts = new TreeGifts();

	public static class Starlyn {
		@SerializedName("personal_bests")
		public PersonalBests personalBests = new PersonalBests();

		public static class PersonalBests {
			public int agatha;
			@SerializedName("FIG_LOG")
			public int figLogs;
			@SerializedName("MANGROVE_LOG")
			public int mangroveLogs;
			public int miria;
			@SerializedName("HELIX_LOG")
			public int helixLogs;
		}
	}

	public static class Hina {
		public Tasks tasks = new Tasks();

		public static class Tasks {
			@SerializedName("completed_tasks")
			public List<String> completedTasks = List.of();
			@SerializedName("task_progress")
			public Map<String, Integer> taskProgress = Map.of();
			@SerializedName("claimed_rewards")
			public List<String> claimedRewards = List.of();
			@SerializedName("tier_claimed")
			public int tierClaimed;
		}
	}

	public static class TreeGifts {
		@SerializedName("FIG")
		public int fig;
		@SerializedName("MANGROVE")
		public int mangrove;
		@SerializedName("HELIX")
		public int helix;

		@SerializedName("milestone_tier_claimed")
		public MilestoneClaimed milestoneClaimed = new MilestoneClaimed();

		public static class MilestoneClaimed {
			@SerializedName("FIG")
			public int fig;
			@SerializedName("MANGROVE")
			public int mangrove;
			@SerializedName("HELIX")
			public int helix;
		}
	}
}
