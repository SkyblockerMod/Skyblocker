package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

public class SkillTree {
	public Nodes nodes = new Nodes();
	@SerializedName("tokens_spent")
	public TokensSpent tokensSpent = new TokensSpent();
	@SerializedName("selected_ability")
	public SelectedAbility selectedAbility = new SelectedAbility();
	public Experience experience = new Experience();
	@SerializedName("selected_skill_tree_slot")
	public SelectedSkillTreeSlot selectedSkillTreeSlot = new SelectedSkillTreeSlot();

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
		@SerializedName("mountain")
		public int mountain1;
		@SerializedName("mountain_2")
		public int mountain2;
		@SerializedName("mountain_3")
		public int mountain3;
		@SerializedName("mountain_4")
		public int mountain4;
		@SerializedName("mountain_5")
		public int mountain5;

		@SerializedName("forest")
		public int forest1;
		@SerializedName("forest_2")
		public int forest2;
		@SerializedName("forest_3")
		public int forest3;
		@SerializedName("forest_4")
		public int forest4;
		@SerializedName("forest_5")
		public int forest5;
	}

	public static class SelectedAbility {
		@SerializedName("mining")
		public String mining1 = "";
		@SerializedName("mining_2")
		public String mining2 = "";
		@SerializedName("mining_3")
		public String mining3 = "";
		@SerializedName("mining_4")
		public String mining4 = "";
		@SerializedName("mining_5")
		public String mining5 = "";

		@SerializedName("foraging")
		public String foraging1 = "";
		@SerializedName("foraging_2")
		public String foraging2 = "";
		@SerializedName("foraging_3")
		public String foraging3 = "";
		@SerializedName("foraging_4")
		public String foraging4 = "";
		@SerializedName("foraging_5")
		public String foraging5 = "";
	}

	public static class Experience {
		public double mining;
		public double foraging;
	}

	public static class SelectedSkillTreeSlot {
		public int mining = 1;
		public int foraging = 1;
	}

	@SerializedName("mining")
	public SkillTreeProperties mining1Properties = new SkillTreeProperties();
	@SerializedName("mining_2")
	public SkillTreeProperties mining2Properties = new SkillTreeProperties();
	@SerializedName("mining_3")
	public SkillTreeProperties mining3Properties = new SkillTreeProperties();
	@SerializedName("mining_4")
	public SkillTreeProperties mining4Properties = new SkillTreeProperties();
	@SerializedName("mining_5")
	public SkillTreeProperties mining5Properties = new SkillTreeProperties();

	@SerializedName("foraging")
	public SkillTreeProperties foraging1Properties = new SkillTreeProperties();
	@SerializedName("foraging_2")
	public SkillTreeProperties foraging2Properties = new SkillTreeProperties();
	@SerializedName("foraging_3")
	public SkillTreeProperties foraging3Properties = new SkillTreeProperties();
	@SerializedName("foraging_4")
	public SkillTreeProperties foraging4Properties = new SkillTreeProperties();
	@SerializedName("foraging_5")
	public SkillTreeProperties foraging5Properties = new SkillTreeProperties();

	public static class SkillTreeProperties {
		@SerializedName("custom_name")
		public @Nullable String customName;
	}
}
