package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

public class SkillTree {
	public Nodes nodes = new Nodes();
	@SerializedName("tokens_spent")
	public TokensSpent tokensSpent = new TokensSpent();
	public Experience experience = new Experience();

	public static class Nodes {
		public Mining mining = new Mining();
		public Foraging foraging = new Foraging();

		public static class Mining {
			@SerializedName("core_of_the_mountain")
			public int coreOfTheMountain;
		}

		public static class Foraging {
			@SerializedName("center_of_the_forest")
			public int centreOfTheForest;
		}
	}

	public static class TokensSpent {
		public int mountain;
		public int forest;
	}

	public static class Experience {
		public int mining;
		public int foraging;
	}
}
