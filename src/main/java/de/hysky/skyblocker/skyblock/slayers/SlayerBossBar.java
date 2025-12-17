package de.hysky.skyblocker.skyblock.slayers;

import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerBossBar {
	private static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?[kM]?)(?=❤)");
	private static int bossMaxHealth = -1;
	private static @Nullable ClientBossBar bossBar;

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
			ArmorStandEntity bossArmorStand = SlayerManager.getSlayerBossArmorStand();
			assert bossArmorStand != null;
			Matcher maxHealthMatcher = HEALTH_PATTERN.matcher(bossArmorStand.getName().getString());
			if (maxHealthMatcher.find()) bossMaxHealth = convertToInt(maxHealthMatcher.group(0));
		}

		return true;
	}

	/**
	 * Updates the boss bar with the current slayer's health, called every frame.
	 *
	 * @return The updated boss bar.
	 */
	public static ClientBossBar updateBossBar() {
		ArmorStandEntity slayer = SlayerManager.getSlayerBossArmorStand();
		assert slayer != null;

		if (bossBar == null) bossBar = new ClientBossBar(UUID.randomUUID(), slayer.getDisplayName(), 1f, BossBar.Color.PURPLE, BossBar.Style.PROGRESS, false, false, false);

		// Update the boss bar with the current slayer's health
		Matcher healthMatcher = HEALTH_PATTERN.matcher(slayer.getName().getString());
		if (healthMatcher.find()) {
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
