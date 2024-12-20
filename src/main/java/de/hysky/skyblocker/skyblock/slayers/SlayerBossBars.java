package de.hysky.skyblocker.skyblock.slayers;

import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerBossBars {
	public static final UUID UUID = java.util.UUID.randomUUID();
	private static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?[kM]?)(?=❤)");
	private static final long UPDATE_INTERVAL = 400;
	private static int bossMaxHealth = -1;
	private static long lastUpdateTime = 0;
	private static ClientBossBar bossBar;

	/**
	 * Determines if the boss bar should be rendered and updates the max health of the boss.
	 * Has a 400ms cooldown built-in.
	 */
	public static boolean shouldRenderBossBar() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
			return bossBar != null;
		}
		lastUpdateTime = currentTime;

		// Reset if no slayer
		if (!SlayerManager.isBossSpawned()) {
			bossMaxHealth = -1;
			bossBar = null;
			return false;
		}

		// Update boss max health
		ArmorStandEntity bossArmorStand = SlayerManager.getSlayerBossArmorStand();
		if (bossArmorStand != null && bossMaxHealth == -1) {
			Matcher maxHealthMatcher = HEALTH_PATTERN.matcher(bossArmorStand.getName().getString());
			if (maxHealthMatcher.find()) bossMaxHealth = convertToInt(maxHealthMatcher.group(0));
		}

		return bossBar != null || bossArmorStand != null;
	}

	/**
	 * Updates the boss bar with the current slayer's health, called every frame.
	 *
	 * @return The updated boss bar.
	 */
	public static ClientBossBar updateBossBar() {
		ArmorStandEntity slayer = SlayerManager.getSlayerBossArmorStand();
		if (bossBar == null) bossBar = new ClientBossBar(UUID, slayer != null ? slayer.getDisplayName() : Text.of("Attempting to Locate Slayer..."), 1f, BossBar.Color.PURPLE, BossBar.Style.PROGRESS, false, false, false);

		// If no slayer armor stand is found, display a red progress bar
		if (slayer == null) {
			bossBar.setStyle(BossBar.Style.PROGRESS);
			bossBar.setColor(BossBar.Color.RED);
			return bossBar;
		}

		// Update the boss bar with the current slayer's health
		Matcher healthMatcher = HEALTH_PATTERN.matcher(slayer.getName().getString());
		if (healthMatcher.find() && slayer.isAlive()) {
			bossBar.setPercent(bossMaxHealth == -1 ? 1f : (float) convertToInt(healthMatcher.group(1)) / bossMaxHealth);
			bossBar.setColor(BossBar.Color.PINK);
			bossBar.setName(slayer.getDisplayName());
			bossBar.setStyle(BossBar.Style.NOTCHED_10);
		} else {
			bossBar.setColor(BossBar.Color.RED);
			bossBar.setStyle(BossBar.Style.PROGRESS);
			bossBar.setName(slayer.getDisplayName());
		}

		return bossBar;
	}

	private static int convertToInt(String value) {
		if (value == null || value.isEmpty()) {
			return 0;
		}

		value = value.replace(",", "").trim().toLowerCase(Locale.ENGLISH);
		double multiplier = 1.0;

		if (value.endsWith("m")) {
			multiplier = 1_000_000;
			value = value.substring(0, value.length() - 1);
		} else if (value.endsWith("k")) {
			multiplier = 1_000;
			value = value.substring(0, value.length() - 1);
		}

		try {
			double numericValue = Double.parseDouble(value);
			return (int) (numericValue * multiplier);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
