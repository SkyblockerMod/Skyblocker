package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PlayerStats {
	@SerializedName("sea_creature_kills")
	public int seaCreatureKills;
	@SerializedName("glowing_mushrooms_broken")
	public float glowingMushroomsBroken;
	@SerializedName("highest_damage")
	public long highestDamage;
	@SerializedName("highest_critical_damage")
	public long highestCriticalDamage;

	// candy_collected
	/**
	 * Has a {@code total} field and does not distinguish between levels. Not sure if this is updated with new kills after the bestiary data.
	 *
	 * @see Bestiary#kills
	 */
	public Map<String, Integer> kills = Map.of();
	/**
	 * Has a {@code total} field and does not distinguish between levels. Not sure if this is updated with new deaths after the bestiary data.
	 *
	 * @see Bestiary#deaths
	 */
	public Map<String, Integer> deaths = Map.of();

	public Pets pets = new Pets();

	public static class Pets {
		@SerializedName("total_exp_gained")
		public double totalExpGained;

		public Milestone milestone = new Milestone();

		public static class Milestone {
			@SerializedName("sea_creatures_killed")
			public int seaCreaturesKilled;
			@SerializedName("ores_mined")
			public int oresMined;
		}
	}

	public Gifts gifts = new Gifts();

	public static class Gifts {
		@SerializedName("total_given")
		public int totalGiven;
		@SerializedName("total_received")
		public int totalReceived;
	}

	public Auctions auctions = new Auctions();

	public static class Auctions {
		@SerializedName("highest_bid")
		public double highestBid;
		public int bids;
		public int won;
		public int created;
		@SerializedName("gold_spent")
		public double goldSpent;
		@SerializedName("gold_earned")
		public double goldEarned;
		@SerializedName("no_bids")
		public int noBids;
		public double fees;

		@SerializedName("total_bought")
		public RarityStats totalBought = new RarityStats();
		@SerializedName("total_sold")
		public RarityStats totalSold = new RarityStats();

		public static class RarityStats {
			@SerializedName("COMMON")
			public int common;
			@SerializedName("UNCOMMON")
			public int uncommon;
			@SerializedName("RARE")
			public int rare;
			@SerializedName("EPIC")
			public int epic;
			@SerializedName("LEGENDARY")
			public int legendary;
			@SerializedName("MYTHIC")
			public int mythic;
			@SerializedName("ULTIMATE")
			public int ultimate;
			@SerializedName("SPECIAL")
			public int special;
			public int total;
		}
	}

	public ItemsFished items_fished = new ItemsFished();

	public static class ItemsFished {
		public int total;
		public int normal;
		public int treasure;
		@SerializedName("large_treasure")
		public int largeTreasure;
		@SerializedName("trophy_fish")
		public int trophyFish;
		public int outstanding;
	}

	public Rift rift = new Rift();

	public static class Rift {
		@SerializedName("visits")
		public int visits;
		@SerializedName("pass_consumed")
		public int passConsumed;
		@SerializedName("lifetime_motes_earned")
		public int lifetimeMotesEarned;
		@SerializedName("motes_orb_pickup")
		public int motesOrbPickup;
		@SerializedName("lagoon_leech_supreme_killed")
		public int leechSupremeKills;
		@SerializedName("colosseum_bacte_defeated")
		public int bacteKills;
		@SerializedName("living_cave_snake_collected")
		public int livingCaveSnakesCollected;
		@SerializedName("castle_sent_to_prison")
		public int timesSentToPrison;
		@SerializedName("castle_effigy_broken")
		public int castleEffigiesBroken;
	}

	@SerializedName("mythos")
	public MythologicalData mythologicalData = new MythologicalData();

	public static class MythologicalData {
		@SerializedName("kills")
		public int kills;
		@SerializedName("burrows_dug_next")
		public BurrowStats burrowsDugNext = new BurrowStats();
		@SerializedName("burrows_dug_combat")
		public BurrowStats burrowsDugCombat = new BurrowStats();
		@SerializedName("burrows_dug_treasure")
		public BurrowStats burrowsDugTreasure = new BurrowStats();
		@SerializedName("burrows_chains_complete")
		public BurrowStats burrowsChainsComplete = new BurrowStats();

		public static class BurrowStats {
			@SerializedName("total")
			public int total;
			@SerializedName("none")
			public int none;
			/** No clue what this field is meant to mean. */
			@SerializedName("null")
			public int nulls;
			@SerializedName("COMMON")
			public int common;
			@SerializedName("UNCOMMON")
			public int uncommon;
			@SerializedName("RARE")
			public int rare;
			@SerializedName("EPIC")
			public int epic;
			@SerializedName("LEGENDARY")
			public int legendary;
		}
	}

	// TODO deaths/kills/winter//races/end_island/spooky/

	@SerializedName("unique_shards")
	public int uniqueShards;
	@SerializedName("shard_combat_hunts")
	public int combatHunts;
	@SerializedName("shard_fishing_hunts")
	public int fishingHunts;
	@SerializedName("shard_forest_hunts")
	public int forestHunts;
	@SerializedName("shard_salt_hunts")
	public int saltHunts;
	@SerializedName("shard_trap_hunts")
	public int trapHunts;
}
