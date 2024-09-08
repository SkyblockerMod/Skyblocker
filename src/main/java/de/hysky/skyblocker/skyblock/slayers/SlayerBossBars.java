package de.hysky.skyblocker.skyblock.slayers;

import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;

import java.util.UUID;

public class SlayerBossBars {
	private static long lastUpdateTime = 0;
	private static final long UPDATE_INTERVAL = 400;
	private static ClientBossBar bossBar;
	public static final UUID uuid = UUID.randomUUID();

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
		if (!Slayer.getInstance().isInSlayerFight()) {
			bossBar = null;
			return false;
		}

		return bossBar != null || Slayer.getInstance().getSlayerArmorStand() != null;
	}

	/**
	 * Updates the boss bar with the current slayer's health, called every frame.
	 *
	 * @return The updated boss bar.
	 */
	public static ClientBossBar updateBossBar() {
		Slayer slayer = Slayer.getInstance();
		if (bossBar == null) bossBar = new ClientBossBar(uuid, slayer.getSlayerArmorStand() != null ? slayer.getSlayerArmorStand().getDisplayName() : Text.of("Attempting to Locate Slayer..."), 1f, BossBar.Color.PURPLE, BossBar.Style.PROGRESS, false, false, false);

		// If no slayer armor stand is found, display a red progress bar
		if (slayer.getSlayerArmorStand() == null) {
			bossBar.setStyle(BossBar.Style.PROGRESS);
			bossBar.setColor(BossBar.Color.RED);
			return bossBar;
		}

		// Update the boss bar with the current slayer's health
		bossBar.setName(slayer.getSlayerArmorStand().getDisplayName());
		if (slayer.getSlayerArmorStand().isAlive()) {
			bossBar.setPercent(slayer.getMaxHealth() == -1 ? 1f : (float) slayer.getCurrentHealth() / slayer.getMaxHealth());
			bossBar.setColor(BossBar.Color.PINK);
			bossBar.setStyle(BossBar.Style.NOTCHED_10);
		} else {
			bossBar.setColor(BossBar.Color.RED);
			bossBar.setStyle(BossBar.Style.PROGRESS);
		}

		return bossBar;
	}
}
