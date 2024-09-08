package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;

import java.util.regex.Matcher;

import static de.hysky.skyblocker.utils.SlayerUtils.*;
import static de.hysky.skyblocker.utils.Utils.abbrNumberStringToLong;

public class Slayer {
	private static boolean isInSlayerQuest;
	private static boolean isInSlayerFight;
	private static String bossType;
	private static int bossTier;
	private static long maxHealth = -1;
	private static long currentHealth;
	private static ArmorStandEntity slayerArmorStand;

	private static Slayer instance;

	// singleton pattern constructor
	private Slayer() {}

	@Init
	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(Slayer::onTick);
	}

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

		slayerArmorStand = getSlayerArmorStandEntity();
		return slayerArmorStand;
	}

	public void setInSlayerQuest(boolean inSlayerQuest) {
		isInSlayerQuest = inSlayerQuest;
	}

	public boolean isInSlayerQuest() {
		return isInSlayerQuest;
	}

	public boolean isInSlayerFight() {
		return isInSlayerFight;
	}

	public void setInSlayerFight(boolean inSlayerFight) {
		if (!isInSlayerFight && inSlayerFight) {
			slayerArmorStand = getSlayerArmorStandEntity();
			updateCurrentHealth();
		} else if (isInSlayerFight && !inSlayerFight) {
			maxHealth = -1;
			currentHealth = -1;
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
		Slayer.bossTier = bossTier != null ? Utils.romanToInt(bossTier) : -1;
	}

	public int getBossTier() {
		return bossTier;
	}

	public long getCurrentHealth() {
		return currentHealth;
	}

	private void updateCurrentHealth() {
		;
		if (getSlayerArmorStand() != null && slayerArmorStand.isAlive()) {
			Matcher healthMatcher = HEALTH_PATTERN.matcher(slayerArmorStand.getName().getString());
			if (healthMatcher.find()) {
				currentHealth = abbrNumberStringToLong(healthMatcher.group(1));
			}
		}
	}

	public long getMaxHealth() {
		if (maxHealth != -1) return maxHealth;
		try {
			maxHealth = SLAYER_MOB_MAX_HP.get(bossType)[bossTier - 1];
		} catch (IndexOutOfBoundsException e) {
			Matcher maxHealthMatcher = HEALTH_PATTERN.matcher(getInstance().getSlayerArmorStand().getName().getString());
			if (maxHealthMatcher.find()) {
				maxHealth = abbrNumberStringToLong(maxHealthMatcher.group(0));
			} else {
				maxHealth = -1;
			}
		}
		return maxHealth;
	}

	private ArmorStandEntity getSlayerArmorStandEntity() {
		if (MinecraftClient.getInstance().world != null) {
			for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
				if (entity.hasCustomName()) {
					String entityName = entity.getCustomName().getString();
					Matcher matcher = SLAYER_PATTERN.matcher(entityName);
					if (matcher.find()) {
						String username = MinecraftClient.getInstance().getSession().getUsername();
						for (Entity armorStand : getEntityArmorStands(entity, 1.5f)) {
							if (armorStand.getDisplayName().getString().contains(username)) {
								return (ArmorStandEntity) entity;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static void onTick(MinecraftClient minecraftClient) {
		if (Slayer.getInstance().isInSlayerQuest()) {
			Slayer.getInstance().updateCurrentHealth();
		}
	}
}
