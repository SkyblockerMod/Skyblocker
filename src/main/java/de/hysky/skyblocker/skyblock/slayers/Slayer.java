package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import java.util.regex.Matcher;

import static de.hysky.skyblocker.utils.SlayerUtils.*;
import static de.hysky.skyblocker.utils.Utils.abbrNumberStringToLong;

public class Slayer {
	private boolean isInSlayerQuest = false;
	private boolean isInSlayerFight = false;
	private String bossType;
	private int bossTier;
	private long maxHealth = -1;
	private long currentHealth;
	private ArmorStandEntity slayerArmorStand;

	private static Slayer instance;

	// singleton pattern constructor
	private Slayer() {}

	@Init
	public static void init() {
		instance = new Slayer();
		ClientTickEvents.START_CLIENT_TICK.register(Slayer::onTick);
	}

	public static Slayer getInstance() {
		return instance;
	}

	public ArmorStandEntity getSlayerArmorStand() {
		if (slayerArmorStand != null && slayerArmorStand.isAlive()) {
			return slayerArmorStand;
		}

		slayerArmorStand = getSlayerArmorStandEntity();
		return slayerArmorStand;
	}

	public void updateSlayerInfo(boolean inQuest, boolean inFight, String bossType, String bossTier) {
		if (!isInSlayerFight && inFight) {
			detectAndScheduleArmorStandCheck();
			updateCurrentHealth();
		} else if (isInSlayerFight && !inFight) {
			maxHealth = -1;
			currentHealth = -1;
		}

		this.isInSlayerQuest = inQuest;
		this.isInSlayerFight = inFight;
		this.bossType = bossType;
		this.bossTier = bossTier != null ? Utils.romanToInt(bossTier) : -1;
	}

	private void detectAndScheduleArmorStandCheck() {
		slayerArmorStand = getSlayerArmorStandEntity();

		if (slayerArmorStand != null) {
			Scheduler.INSTANCE.schedule(() -> {
				ArmorStandEntity secondDetectedStand = getSlayerArmorStandEntity();

				if (secondDetectedStand != null && !isSameArmorStand(slayerArmorStand, secondDetectedStand)) {
					Scheduler.INSTANCE.schedule(() -> {
						ArmorStandEntity thirdDetectedStand = getSlayerArmorStandEntity();

						slayerArmorStand = finalizeArmorStandDetection(slayerArmorStand, secondDetectedStand, thirdDetectedStand);
					}, 20, false);
				}
			}, 30, false);
		}
	}

	private ArmorStandEntity finalizeArmorStandDetection(ArmorStandEntity stand1, ArmorStandEntity stand2, ArmorStandEntity stand3) {
		if (isSameArmorStand(stand1, stand2)) {
			return stand1;
		} else if (isSameArmorStand(stand2, stand3)) {
			return stand2;
		} else if (isSameArmorStand(stand1, stand3)) {
			return stand1;
		} else {
			return null;
		}
	}

	private boolean isSameArmorStand(ArmorStandEntity stand1, ArmorStandEntity stand2) {
		return stand1 != null && stand2 != null && stand1.getUuid().equals(stand2.getUuid());
	}

	public String getBossType() {
		return bossType;
	}

	public boolean isBossType(String slayer) {
		return isInSlayerQuest && bossType != null && bossType.equals(slayer);
	}

	public int getBossTier() {
		return bossTier;
	}

	public boolean isInSlayerQuest() {
		return isInSlayerQuest;
	}

	public boolean isInSlayerFight() {
		return isInSlayerFight;
	}

	public long getCurrentHealth() {
		return currentHealth;
	}

	public long getMaxHealth() {
		if (maxHealth != -1) return maxHealth;

		try {
			maxHealth = SLAYER_MOB_MAX_HP.get(bossType)[bossTier - 1];
		} catch (Exception ignored) {
			Matcher maxHealthMatcher = HEALTH_PATTERN.matcher(getInstance().getSlayerArmorStand().getName().getString());
			maxHealth = maxHealthMatcher.find() ? abbrNumberStringToLong(maxHealthMatcher.group(0)) : -1;
		}

		return maxHealth;
	}

	private void updateCurrentHealth() {
		if (getSlayerArmorStand() != null && slayerArmorStand.isAlive()) {
			Matcher healthMatcher = HEALTH_PATTERN.matcher(slayerArmorStand.getName().getString());
			if (healthMatcher.find()) {
				currentHealth = abbrNumberStringToLong(healthMatcher.group(1));
			}
		}
	}

	private ArmorStandEntity getSlayerArmorStandEntity() {
		if (MinecraftClient.getInstance().world != null) {
			for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
				if (entity instanceof ArmorStandEntity && entity.hasCustomName()) {
					String armorStandName = entity.getName().getString();
					Matcher matcher = SLAYER_PATTERN.matcher(armorStandName);

					if (matcher.find()) {
						String detectedBossType = matcher.group(1);
						String detectedBossTier = matcher.group(2);
						int detectedTier = Utils.romanToInt(detectedBossTier);

						if (detectedBossType.equals(bossType) && detectedTier == bossTier) {
							String username = MinecraftClient.getInstance().getSession().getUsername();

							for (Entity armorStand : getEntityArmorStands(entity, 1.5f)) {
								if (armorStand.getName().getString().contains(username)) {
									return (ArmorStandEntity) entity;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static void onTick(MinecraftClient minecraftClient) {
		if (Slayer.getInstance().isInSlayerFight) Slayer.getInstance().updateCurrentHealth();
	}
}
