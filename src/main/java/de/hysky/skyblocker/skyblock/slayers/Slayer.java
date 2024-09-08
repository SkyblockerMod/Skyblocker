package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.utils.SlayerUtils;
import net.minecraft.entity.decoration.ArmorStandEntity;

public class Slayer {
	private static boolean isInSlayerQuest;
	private static boolean isInSlayerFight;
	private static String bossType;
	private static String bossTier;
	private static ArmorStandEntity slayerArmorStand;

	private static Slayer instance;

	// singleton pattern constructor
	private Slayer() {}

	public static synchronized Slayer getInstance() {
		if (instance == null) {
			instance = new Slayer();
		}
		return instance;
	}

	public ArmorStandEntity getSlayerArmorStand() {
		if (slayerArmorStand != null && slayerArmorStand.isAlive()) {
			return slayerArmorStand;
		}

		slayerArmorStand = SlayerUtils.getSlayerArmorStandEntity();
		return slayerArmorStand;
	}

	public void setInSlayerQuest(boolean inSlayerQuest) {
		isInSlayerQuest = inSlayerQuest;
	}

	public boolean isInSlayerFight() {
		return isInSlayerFight;
	}

	public void setInSlayerFight(boolean inSlayerFight) {
		if (!isInSlayerFight && inSlayerFight) {
			slayerArmorStand = SlayerUtils.getSlayerArmorStandEntity();
		}
		isInSlayerFight = inSlayerFight;
	}

	public String getBossType() {
		return bossType;
	}

	public boolean isBossType(String slayer) {
		return isInSlayerQuest && bossType.equals(slayer);
	}

	public void setBossType(String bossType) {
		Slayer.bossType = bossType;
	}

	public void setBossTier(String bossTier) {
		Slayer.bossTier = bossTier;
	}
}

