package de.hysky.skyblocker.skyblock.slayers;

import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerBossBar {
	private static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?[kM]?)(?=❤)");
	private static int bossMaxHealth = -1;
	private static @Nullable LerpingBossEvent bossBar;

	/**
	 * Determines if the boss bar should be rendered and updates the max health of the boss.
	 */
	public static boolean shouldRenderBossBar() {
		if (!SlayerManager.isFightingSlayer()) {
			bossBar = null;
			bossMaxHealth = -1;
			return false;
		}

		// Update boss max health
		if (bossMaxHealth == -1) {
			SlayerManager.BossFight bossFight = SlayerManager.getBossFight();
			if (bossFight != null) {
				String bossName = bossFight.armorStand.getName().getString();
				Matcher maxHealthMatcher = HEALTH_PATTERN.matcher(bossName);
				if (maxHealthMatcher.find()) {
					int currentHealth = convertToInt(maxHealthMatcher.group(0));
					int maxHealth = bossFight.slayerType.getHealth(bossName, bossFight.slayerTier);
					bossMaxHealth = Math.max(maxHealth, currentHealth);
				}
			}
		}

		return true;
	}

	/**
	 * Updates the boss bar with the current slayer's health, called every frame.
	 *
	 * @return The updated boss bar.
	 */
	public static LerpingBossEvent updateBossBar() {
		ArmorStand slayerArmorStand = SlayerManager.getSlayerArmorStand();
		assert slayerArmorStand != null;
		Component name = slayerArmorStand.getName();

		if (bossBar == null) bossBar = new LerpingBossEvent(UUID.randomUUID(), name, 1f, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS, false, false, false);

		// Update the boss bar with the current slayerArmorStand's health
		Matcher healthMatcher = HEALTH_PATTERN.matcher(name.getString());
		if (healthMatcher.find()) {
			int health = convertToInt(healthMatcher.group(1));
			if (health > bossMaxHealth) bossMaxHealth = health;
			bossBar.setProgress(bossMaxHealth < 1 ? 1f : (float) health / bossMaxHealth);
			bossBar.setColor(BossEvent.BossBarColor.PINK);
			bossBar.setName(name);
			bossBar.setOverlay(BossEvent.BossBarOverlay.NOTCHED_10);
		} else {
			bossBar.setColor(BossEvent.BossBarColor.RED);
			bossBar.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
			bossBar.setName(name);
		}

		return bossBar;
	}

	private static int convertToInt(@Nullable String value) {
		if (value == null || value.isEmpty()) return 0;

		value = value.replace(",", "").trim().toLowerCase(Locale.ENGLISH);
		int multiplier = value.endsWith("m") ? 1_000_000 : value.endsWith("k") ? 1_000 : 1;
		if (multiplier > 1) value = value.substring(0, value.length() - 1);

		try {
			double numericValue = Double.parseDouble(value);
			return (int) (numericValue * multiplier);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
