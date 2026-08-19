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
	public Honey honey = new Honey();

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

	public static class Honey {
		@SerializedName("refill_times")
		public RefillTimes honeyhiveRefillTimes = new RefillTimes();
		@SerializedName("smeared_trees")
		public SmearedTrees smearedTrees = new SmearedTrees();

		/// Each field contains the timestamp for when the hive will refill.
		public static class RefillTimes {
			/// The hive at -729, 129, 187
			@SerializedName("hive_1")
			public long hive1;

			/// The hive at -733, 128, 188
			@SerializedName("hive_2")
			public long hive2;

			/// The hive at -724, 92, 204
			@SerializedName("hive_3")
			public long hive3;

			/// The hive at -721, 92, 207
			@SerializedName("hive_4")
			public long hive4;

			/// The hive at -724, 93, 211
			@SerializedName("hive_5")
			public long hive5;

			/// The hive at -707, 92, 220
			@SerializedName("hive_6")
			public long hive6;

			/// The hive at -705, 92, 225
			@SerializedName("hive_7")
			public long hive7;

			/// The hive at -693, 94, 147
			@SerializedName("hive_8")
			public long hive8;

			/// The hive at -696, 93, 150
			@SerializedName("hive_9")
			public long hive9;

			/// The hive at -693, 93, 153
			@SerializedName("hive_10")
			public long hive10;

			/// The hive at -665, 97, 167
			@SerializedName("hive_11")
			public long hive11;

			/// The hive at -664, 96, 170
			@SerializedName("hive_12")
			public long hive12;

			/// The hive at -606, 98, 275
			@SerializedName("hive_13")
			public long hive13;

			/// The hive at -611, 98, 274
			@SerializedName("hive_14")
			public long hive14;

			/// The hive at -588, 150, 257
			@SerializedName("hive_15")
			public long hive15;

			/// The hive at -581, 152, 258
			@SerializedName("hive_16")
			public long hive16;

			/// The hive at -578, 151, 256
			@SerializedName("hive_17")
			public long hive17;

			/// The hive at -577, 102, 205
			@SerializedName("hive_18")
			public long hive18;

			/// The hive at -572, 101, 206
			@SerializedName("hive_19")
			public long hive19;
		}

		/// Each field contains the timestamp for when a critter will appear at the location.
		public static class SmearedTrees {
			/// The fig tree near the edge of Tangleburg at -606, 114, 9
			@SerializedName("fig_1")
			public long fig1;

			/// The fig tree near the edge of the West Reaches at -732, 120, 37
			@SerializedName("fig_2")
			public long fig2;

			/// The fig tree near the edge of the North Reaches at -663, 114, -79
			@SerializedName("fig_3")
			public long fig3;

			/// The mangrove tree near the Torrhus Canyon portal at -716, 96, 41
			@SerializedName("mangrove_1")
			public long mangrove1;

			/// The mangrove tree near the Murkwater Loch at -615, 89, 30
			@SerializedName("mangrove_2")
			public long mangrove2;

			/// The mangrove tree near the edge of the South Wetlands at -609, 96, 93
			@SerializedName("mangrove_3")
			public long mangrove3;

			/// The helix tree at -512, 108, 259
			@SerializedName("helix_1")
			public long helix1;

			/// The helix tree near Honey at -619, 97, 235
			@SerializedName("helix_2")
			public long helix2;

			/// The helix tree at -549, 110, 299
			@SerializedName("helix_3")
			public long helix3;

			/// The huge helix tree at -540, 111, 284
			@SerializedName("helix_mega")
			public long helixMega;
		}
	}
}
