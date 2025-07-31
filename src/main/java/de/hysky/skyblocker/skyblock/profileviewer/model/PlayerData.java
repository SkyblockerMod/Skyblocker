package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerData {
	@SerializedName("visited_zones")
	public Set<String> visitedZones = Set.of();
	/**
	 * Seems to be a second timestamp with some sort of offset.
	 */
	@SerializedName("last_death")
	public long lastDeath;
	@SerializedName("perks")
	public Map<String, Integer> essencePerks = Map.of();
	@SerializedName("active_effects")
	public List<Effect> activeEffects = List.of();
	@SerializedName("reaper_peppers_eaten")
	public int reaperPeppersEaten;
	@SerializedName("death_count")
	public int deathCount;
	@SerializedName("disabled_potion_effects")
	public Set<String> disabledPotionEffects = Set.of();
	/**
	 * Probably the set of miniature islands that have been spawned. Is this a list of all of them or just the ones that have achievements associated?
	 */
	@SerializedName("achievement_spawned_island_types")
	public Set<String> spawnedIslandTypes = Set.of();
	@SerializedName("visited_modes")
	public Set<String> visitedIslandModes = Set.of();

	/**
	 * Collection tiers in the format of {@code [collection]_[level]}. Does contain some nonsense levels like {@code -1}. What's up with those? Also those can be derived from adding up {@link ProfileMember#collection} for all players.
	 */
	@SerializedName("unlocked_coll_tiers")
	public Set<String> unlockedCollectionTiers = Set.of();

	/**
	 * All minion tiers crafted, in the form of {@code [resource]_[level]}
	 *
	 * @see #getMinionTier
	 * @see #hasCraftedMinionTier
	 */
	@SerializedName("crafted_generators")
	public Set<String> craftedMinions = Set.of();
	@SerializedName("fishing_treasure_caught")
	public int fishingTreasuresCaught;
	public Map<String, Double> experience = Map.of();


	/**
	 * @param tier one indexed minion tier
	 */
	public boolean hasCraftedMinionTier(String minionType, int tier) {
		return craftedMinions.contains(String.format("%s_%d", minionType, tier));
	}

	public double getSkillExperience(Skill skill) {
		return experience.getOrDefault("SKILL_" + skill.name(), 0.0);
	}

	public LevelFinder.LevelInfo getSkillLevel(Skill skill) {
		return LevelFinder.getLevelInfo(skill.levelFinderOverride, (long) getSkillExperience(skill));
	}

	public enum Skill {
		FISHING("Fishing", Ico.FISH_ROD),
		ALCHEMY("Alchemy", Ico.BREWING_STAND),
		//		DUNGEONEERING,
		RUNECRAFTING("Runecrafting", Ico.MAGMA_CREAM, "Runecraft"),
		MINING("Mining", Ico.STONE_PICKAXE),
		FARMING("Farming", Ico.GOLDEN_HOE),
		ENCHANTING("Enchanting", Ico.ENCHANTING_TABLE),
		TAMING("Taming", Ico.BONE),
		FORAGING("Foraging", Ico.JUNGLE_SAPLING),
		SOCIAL("Social", Ico.EMERALD, "Social"),
		CARPENTRY("Carpentry", Ico.CRAFTING_TABLE),
		CATACOMBS("Catacombs", Ico.SKULL, "Catacombs"),
		COMBAT("Combat", Ico.STONE_SWORD),
		;
		private final String name;
		private final ItemStack itemStack;
		private final String levelFinderOverride;


		Skill(String name, ItemStack itemStack) {
			this(name, itemStack, "GenericSkill");
		}

		Skill(String name, ItemStack itemStack, String override) {
			this.name = name;
			this.itemStack = itemStack;
			this.levelFinderOverride = override;
		}

		public LevelFinder.LevelInfo getLevelInfo(double experience) {
			return LevelFinder.getLevelInfo(levelFinderOverride, (long) experience);
		}

		public String getName() {
			return name;
		}

		public ItemStack getIcon() {
			return itemStack;
		}
	}


	/**
	 * Gets the highest contiguously unlocked minion tier. It is still possible to craft a higher tier minion by trading with other players.
	 *
	 * @return the one indexed highest crafted minion tier contiguously from tier 1.
	 */
	public int getMinionTier(String resource) { // TODO: are minion crafts shared between players in a profile
		int i = 1;
		for (; i < 15; i++) { // Let's go for 15 for future proofing!
			if (!hasCraftedMinionTier(resource, i))
				break;
		}
		return i - 1;
	}

	public static class Effect {
		@SerializedName("effect")
		public String effectId;
		public int level = 1;
		// "modifiers": []
		@SerializedName("ticks_remaining")
		public long ticksRemaining;
		public boolean infinite;
		public int flags;
	}
}
