package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.world.item.ItemStack;

public enum Skill {
	ALCHEMY("Alchemy", 50, Ico.BREWING_STAND),
	CARPENTRY("Carpentry", 50, Ico.CRAFTING_TABLE),
	CATACOMBS("Catacombs", 50, Ico.CATACOMBS),
	COMBAT("Combat", 60, Ico.STONE_SWORD),
	ENCHANTING("Enchanting", 60, Ico.ENCHANTING_TABLE),
	FARMING("Farming", 50, Ico.GOLDEN_HOE),
	FISHING("Fishing", 50, Ico.FISH_ROD),
	FORAGING("Foraging", 50, Ico.JUNGLE_SAPLING),
	HUNTING("Hunting", 25, Ico.LEAD),
	MINING("Mining", 60, Ico.STONE_PICKAXE),
	RUNECRAFTING("Runecrafting", 25, Ico.MAGMA_CREAM),
	SOCIAL("Social", 25, Ico.EMERALD),
	TAMING("Taming", 50, Ico.BONE);

	private final String name;
	private final int baseCap;
	private final ItemStack icon;

	Skill(String name, int baseCap, ItemStack itemStack) {
		this.name = name;
		this.baseCap = baseCap;
		this.icon = itemStack;
	}

	public LevelInfo getLevelInfo(double experience) {
		return LevelCalculator.getSkillLevel((long) experience, this);
	}

	public String getName() {
		return this.name;
	}

	public int baseCap() {
		return this.baseCap;
	}

	public ItemStack getIcon() {
		return this.icon;
	}
}
