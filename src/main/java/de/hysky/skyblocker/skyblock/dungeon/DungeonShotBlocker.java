package de.hysky.skyblocker.skyblock.dungeon;

import net.minecraft.client.player.LocalPlayer;

import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonBlaze;

/**
 * Gate for the client attack handler. Shortbows fire on left click and their shots fly past block
 * reach, so the attack callbacks never see them; this is the one place the mixin can ask whether
 * a left click should fail instead.
 */
public final class DungeonShotBlocker {
	private DungeonShotBlocker() {}

	public static boolean shouldBlockLeftClick(LocalPlayer player) {
		return DungeonBlaze.shouldBlockLeftClickShot(player);
	}
}
