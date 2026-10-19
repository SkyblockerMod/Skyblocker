package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.Map;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.tree.SkillTreeBuilder;

public class SkillTree {
	public Nodes nodes = new Nodes();
	@SerializedName("tokens_spent")
	public TokensSpent tokensSpent = new TokensSpent();
	@SerializedName("selected_ability")
	public SelectedAbility selectedAbility = new SelectedAbility();
	public Experience experience = new Experience();
	@SerializedName("selected_skill_tree_slot")
	public SelectedSkillTreeSlot selectedSkillTreeSlot = new SelectedSkillTreeSlot();

	public boolean unlockedMiningTree(int slot) {
		return slot <= SkillTreeBuilder.FREE_SLOTS || this.tokensSpent.getMountainTokensSpent(slot) > 0;
	}

	public boolean unlockedForagingTree(int slot) {
		return slot <= SkillTreeBuilder.FREE_SLOTS || this.tokensSpent.getForestTokensSpent(slot) > 0;
	}

	public static class Nodes {
		@SerializedName("mining")
		public Map<String, Object> mining1 = Map.of();
		@SerializedName("mining_2")
		public Map<String, Object> mining2 = Map.of();
		@SerializedName("mining_3")
		public Map<String, Object> mining3 = Map.of();
		@SerializedName("mining_4")
		public Map<String, Object> mining4 = Map.of();
		@SerializedName("mining_5")
		public Map<String, Object> mining5 = Map.of();

		public Map<String, Object> getMiningNode(int slot) {
			return switch (slot) {
				case 1 -> this.mining1;
				case 2 -> this.mining2;
				case 3 -> this.mining3;
				case 4 -> this.mining4;
				case 5 -> this.mining5;
				default -> throw new IllegalArgumentException("Slot must be between 1-5.");
			};
		}

		@SerializedName("foraging")
		public Map<String, Object> foraging1 = Map.of();
		@SerializedName("foraging_2")
		public Map<String, Object> foraging2 = Map.of();
		@SerializedName("foraging_3")
		public Map<String, Object> foraging3 = Map.of();
		@SerializedName("foraging_4")
		public Map<String, Object> foraging4 = Map.of();
		@SerializedName("foraging_5")
		public Map<String, Object> foraging5 = Map.of();

		public Map<String, Object> getForagingNode(int slot) {
			return switch (slot) {
				case 1 -> this.foraging1;
				case 2 -> this.foraging2;
				case 3 -> this.foraging3;
				case 4 -> this.foraging4;
				case 5 -> this.foraging5;
				default -> throw new IllegalArgumentException("Slot must be between 1-5.");
			};
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

		public int getMountainTokensSpent(int slot) {
			return switch (slot) {
				case 1 -> this.mountain1;
				case 2 -> this.mountain2;
				case 3 -> this.mountain3;
				case 4 -> this.mountain4;
				case 5 -> this.mountain5;
				default -> throw new IllegalArgumentException("Slot must be between 1-5.");
			};
		}

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

		public int getForestTokensSpent(int slot) {
			return switch (slot) {
				case 1 -> this.forest1;
				case 2 -> this.forest2;
				case 3 -> this.forest3;
				case 4 -> this.forest4;
				case 5 -> this.forest5;
				default -> throw new IllegalArgumentException("Slot must be between 1-5.");
			};
		}
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

		public String getSelectedMiningAbility(int slot) {
			return switch (slot) {
				case 1 -> this.mining1;
				case 2 -> this.mining2;
				case 3 -> this.mining3;
				case 4 -> this.mining4;
				case 5 -> this.mining5;
				default -> throw new IllegalArgumentException("Slot must be between 1-5.");
			};
		}

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

		public String getSelectedForagingAbility(int slot) {
			return switch (slot) {
				case 1 -> this.foraging1;
				case 2 -> this.foraging2;
				case 3 -> this.foraging3;
				case 4 -> this.foraging4;
				case 5 -> this.foraging5;
				default -> throw new IllegalArgumentException("Slot must be between 1-5.");
			};
		}
	}

	public static class Experience {
		public double mining;
		public double foraging;

		public LevelInfo getHotmLevel() {
			return LevelCalculator.getHotmLevel((long) this.mining);
		}

		public LevelInfo getHotfLevel() {
			return LevelCalculator.getHotfLevel((long) this.foraging);
		}
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

	public SkillTreeProperties getMiningTreeProperties(int slot) {
		return switch (slot) {
			case 1 -> this.mining1Properties;
			case 2 -> this.mining2Properties;
			case 3 -> this.mining3Properties;
			case 4 -> this.mining4Properties;
			case 5 -> this.mining5Properties;
			default -> throw new IllegalArgumentException("Slot must be between 1-5.");
		};
	}

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

	public SkillTreeProperties getForagingTreeProperties(int slot) {
		return switch (slot) {
			case 1 -> this.foraging1Properties;
			case 2 -> this.foraging2Properties;
			case 3 -> this.foraging3Properties;
			case 4 -> this.foraging4Properties;
			case 5 -> this.foraging5Properties;
			default -> throw new IllegalArgumentException("Slot must be between 1-5.");
		};
	}

	public static class SkillTreeProperties {
		@SerializedName("custom_name")
		public @Nullable String customName;
	}
}
