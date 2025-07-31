package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

public class Treasures {
	public List<Run> runs = List.of();
	/**
	 * This is a list of chests. Nota bene: the chests are not grouped by dungeon run, but have a run id that can be used to group them.
	 */
	public List<Chest> chests = List.of();
	// TODO: add a method to collate runs and chests

	public static class Chest {
		@SerializedName("run_id")
		public UUID runId;
		@SerializedName("chest_id")
		public UUID chestId;

		/**
		 * The chest type, one of {@code wood}, {@code gold}, {@code diamond}, {@code emerald}, {@code obsidian}, {@code bedrock}.
		 */
		@SerializedName("treasure_type")
		public String treasureType;

		public Rewards rewards = new Rewards();

		public int quality;
		@SerializedName("shiny_eligible")
		public boolean shinyEligible;
		public boolean paid;
		public int rerolls;
	}

	public static class Rewards {
		/**
		 * List of rewards found in a chest. These are not item ids directly:
		 * <ul>
		 *     <li>Enchanted books in the form of {@code combo_1}, {@code feather_falling_6}</li>
		 *     <li>Enchanted ultimate books in the form of {@code wise_1} (without {@code ultimate})</li>
		 *     <li>Essence in the form of {@code ESSENCE:UNDEAD:22}, {@code ESSENCE:WITHER:30}</li>
		 *     <li>Items also have a completely arbitrary form sometimes: {@code master_jerry} {@code dark_orb_1} (with stack size) {@code shadow_boots} (unstackable without stack size, but with differing id from the nbt id)</li>
		 *     <li>Realistically the only way to maintain a mapping for this is a repo file.</li>
		 * </ul>
		 */
		public List<String> rewards = List.of();

		@SerializedName("rolled_rng_meter_randomly")
		public boolean rolledRngMeterRandomly;
	}

	public static class Run {
		@SerializedName("run_id")
		public UUID runId;
		@SerializedName("completion_ts")
		public long completionTimestamp;
		/**
		 * {@code "master_catacombs"} or {@link "catacombs"} TODO: CITATION REQUIRED
		 */
		@SerializedName("dungeon_type")
		public String dungeonType;
		@SerializedName("dungeon_tier")
		public int dungeonTier;
		public List<Participant> participants = List.of();
	}

	public static class Participant {
		@SerializedName("player_uuid")
		public UUID playerUUID;
		/**
		 * This is formatted as {@code [name]: [role] ([class level])} + some colour codes (probably depending on level). Notably the class level is not a roman numeral, but instead an arabic one.
		 */
		@SerializedName("display_name")
		public String display_name;
		@SerializedName("class_milestone")
		public int classMilestone;
	}
}
