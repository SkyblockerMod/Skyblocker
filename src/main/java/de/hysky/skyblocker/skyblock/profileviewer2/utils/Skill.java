package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;

public enum Skill {
	ALCHEMY("Alchemy", 50, Ico.BREWING_STAND),
	CARPENTRY("Carpentry", 50, Ico.CRAFTING_TABLE),
	CATACOMBS("Catacombs", 50, Ico.CATACOMBS),
	COMBAT("Combat", 60, Ico.STONE_SWORD),
	ENCHANTING("Enchanting", 60, Ico.ENCHANTING_TABLE),
	FARMING("Farming", 50, 60, Ico.GOLDEN_HOE),
	FISHING("Fishing", 50, Ico.FISH_ROD),
	FORAGING("Foraging", 50, 54, Ico.JUNGLE_SAPLING),
	HUNTING("Hunting", 25, Ico.LEAD),
	MINING("Mining", 60, Ico.STONE_PICKAXE),
	RUNECRAFTING("Runecrafting", 25, Ico.MAGMA_CREAM),
	SOCIAL("Social", 25, Ico.EMERALD),
	TAMING("Taming", 50, 60, Ico.SPAWN_EGG);

	private final String name;
	private final int baseCap;
	private final int absoluteCap;
	private final FlexibleItemStack icon;

	Skill(String name, int baseCap, FlexibleItemStack icon) {
		this(name, baseCap, baseCap, icon);
	}

	Skill(String name, int baseCap, int absoluteCap, FlexibleItemStack icon) {
		this.name = name;
		this.baseCap = baseCap;
		this.absoluteCap = absoluteCap;
		this.icon = icon;
	}

	public String getFriendlyName() {
		return this.name;
	}

	public int getBaseCap() {
		return this.baseCap;
	}

	public int getAbsoluteCap() {
		return this.absoluteCap;
	}

	public FlexibleItemStack getIcon() {
		return this.icon;
	}
}
