package de.hysky.skyblocker.skyblock.profileviewer2.model;

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
		/**
		 * The type of chest, either {@code DUNGEON} or {@code KUUDRA}.
		 */
		public String type = "";
		@SerializedName("run_id")
		public UUID runId = UUID.randomUUID();
		@SerializedName("chest_id")
		public UUID chestId = UUID.randomUUID();
		/**
		 * The dungeon chest type; one of {@code wood}, {@code gold}, {@code diamond}, {@code emerald}, {@code obsidian}, or {@code bedrock}.
		 */
		@SerializedName("treasure_type")
		public String treasureType = "";
		public Rewards rewards = new Rewards();
		public int quality;
		@SerializedName("shiny_eligible")
		public boolean shinyEligible;
		/** Whether the dungeon chest was paid for. */
		public boolean paid;
		/** This field is for dungeon chests exclusively. */
		public int rerolls;
		/**
		 * The Kuudra chest type; one of {@code free}, or {@code paid}.
		 */
		@SerializedName("chest_type")
		public String chestType = "";
		/**
		 * The Kuudra tier; one of {@code NONE}, {@code HOT}, {@code BURNING}, {@code FIERY}, or {@code INFERNAL}.
		 */
		@SerializedName("run_tier")
		public String kuudraTier = "";
		/**
		 * Seems to be in the format of {@code kuudra_none_tier_paid} for example.
		 */
		@SerializedName("loot_table_id")
		public String lootTableId = "";
		@SerializedName("secondary_slots")
		public int secondarySlots;
		@SerializedName("percentage_completed")
		public int percentageCompleted;
		/** This field is for Kuudra chests exclusively. */
		@SerializedName("is_opened")
		public boolean isOpened;
		/** This field is for Kuudra chests exclusively. */
		@SerializedName("is_rerolled")
		public boolean isRerolled;
		@SerializedName("is_attribute_rerolled")
		public boolean isAttributeRerolled;
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
		/**
		 * The type of run, either {@code DUNGEON} or {@code KUUDRA}.
		 */
		public String type = "";
		@SerializedName("run_id")
		public UUID runId = UUID.randomUUID();
		@SerializedName("completion_ts")
		public long completionTimestamp;
		/**
		 * Either {@code master_catacombs} or {@code catacombs}.
		 */
		@SerializedName("dungeon_type")
		public String dungeonType = "";
		@SerializedName("dungeon_tier")
		public int dungeonTier;
		/**
		 * The Kuudra tier; one of {@code NONE}, {@code HOT}, {@code BURNING}, {@code FIERY}, or {@code INFERNAL}.
		 */
		@SerializedName("tier_id")
		public String kuudraTier = "";
		public List<Participant> participants = List.of();
	}

	public static class Participant {
		@SerializedName("player_uuid")
		public UUID playerUuid;
		/**
		 * This is formatted as {@code [name]: [role] ([class level])} + some colour codes (probably depending on level). Notably the class level is not a roman numeral, but instead an arabic one.
		 */
		@SerializedName("display_name")
		public String displayName;
		/** This field is for dungeon runs exclusively. */
		@SerializedName("class_milestone")
		public int classMilestone;
	}
}
