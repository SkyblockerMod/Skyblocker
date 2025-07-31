package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

public class PlayerStats {
	@SerializedName("sea_creature_kills")
	public int seaCreatureKills;
	@SerializedName("glowing_mushrooms_broken")
	public float glowingMushroomsBroken;
	@SerializedName("highest_damage")
	public long highestDamage;
	@SerializedName("highest_critical_damage")
	public long highestCriticalDamage;

	public Pets pets = new Pets();
	public static class Pets {
		@SerializedName("total_exp_gained")
		public long totalExpGained;

		public Milestone milestone = new Milestone();
		public static class Milestone {
			@SerializedName("sea_creatures_killed")
			public float seaCreaturesKilled;
			@SerializedName("ores_mined")
			public float oresMined;
		}
	}

	public Gifts gifts = new Gifts();
	public static class Gifts {
		@SerializedName("total_received")
		public float totalReceived;
		@SerializedName("total_given")
		public float totalGiven;
	}

	public Auctions auctions = new Auctions();
	public static class Auctions {
		@SerializedName("highest_bid")
		public float highestBid = 0;
		public float bids = 0;
		public float won = 0;
		public float created = 0;
		@SerializedName("goldSpent")
		public float gold_spent = 0;
		@SerializedName("gold_earned")
		public float goldEarned = 0;
		@SerializedName("no_bids")
		public float noBids;
		public float fees;

		@SerializedName("total_bought")
		public RarityStats totalBought = new RarityStats();
		@SerializedName("total_sold")
		public RarityStats totalSold = new RarityStats();

		public static class RarityStats {
			@SerializedName("UNCOMMON")
			public Double uncommon;
			@SerializedName("LEGENDARY")
			public Double legendary;
			@SerializedName("EPIC")
			public Double epic;
			@SerializedName("COMMON")
			public Double common;
			@SerializedName("RARE")
			public Double rare;
			@SerializedName("SPECIAL")
			public Double special;
			@SerializedName("MYTHIC")
			public Double mythic;
			public Double total;
		}
	}

	public ItemsFished items_fished = new ItemsFished();
	public static class ItemsFished {
		public float total = 0;
		public float normal = 0;
		public float treasure = 0;
		@SerializedName("large_treasure")
		public float largeTreasure = 0;
		@SerializedName("trophy_fish")
		public float trophyFish = 0;
	}

	public Rift rift = new Rift();
	public static class Rift {
		@SerializedName("visits")
		public float visits;
		@SerializedName("pass_consumed")
		public float passConsumed;
		@SerializedName("lifetime_motes_earned")
		public float lifetimeMotesEarned;
		@SerializedName("motes_orb_pickup")
		public float motesOrbPickup;
		//TODO Add more data like living_metal_spawnegg_used here if neeeded
	}

	@SerializedName("mythos")
	public MythologicalData mythologicalData = new MythologicalData();
	public static class MythologicalData {
		@SerializedName("kills")
		public float kills;
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
			public float total;
			@SerializedName("none")
			public float none;
			@SerializedName("COMMON")
			public float common;
			@SerializedName("UNCOMMON")
			public float uncommon;
			@SerializedName("RARE")
			public float rare;
			@SerializedName("LEGENDARY")
			public float legendary;
		}
	}

	//TODO deaths/kills/winter//races/end_island/spooky/
}
