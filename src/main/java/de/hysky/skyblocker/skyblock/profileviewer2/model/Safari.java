package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Safari {
	public Tickets tickets = new Tickets();
	@SerializedName("milestone_claimed_tiers")
	public AbstractBiomeData milestoneTiersClaimed = new AbstractBiomeData();
	@SerializedName("biome_captures")
	public AbstractBiomeData biomeCaptures = new AbstractBiomeData();
	@SerializedName("discovered_critters")
	public List<String> discoveredCritters = List.of();
	@SerializedName("discovered_sparkling_critters")
	public List<String> discoveredSparklingCritters = List.of();
	@SerializedName("total_captured_sparkling_critters")
	public int totalSparklingCrittersCaptured;

	public static class Tickets {
		public int basic;
		public int economy;
		public int premium;
		@SerializedName("first_class")
		public int firstClass;
	}

	public static class AbstractBiomeData {
		public int cavern;
		public int forest;
		public int haunted;
		public int icy;
	}
}
