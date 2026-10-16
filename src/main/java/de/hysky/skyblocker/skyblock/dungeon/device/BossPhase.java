package de.hysky.skyblocker.skyblock.dungeon.device;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import de.hysky.skyblocker.annotations.Init;

/**
 * Tracks the device window of an F7 boss fight, armed as soon as Maxor spawns and ending when the
 * core entrance opens, delimited by the bosses' own chat lines. Deliberately independent of the
 * waypoints config so the click blockers keep working when those are disabled.
 */
public final class BossPhase {
	private static final String[] START_MESSAGES = {
			"[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!",
			"[BOSS] Storm: I should have known that I stood no chance."
	};
	private static final String CORE_ENTRANCE = "The Core entrance is opening!";

	private static boolean active;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (overlay) return true;
			String text = message.getString();
			if (text.equals(CORE_ENTRANCE)) {
				active = false;
			} else {
				for (String start : START_MESSAGES) {
					if (text.equals(start)) {
						active = true;
						break;
					}
				}
			}
			return true;
		});
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> active = false);
	}

	static boolean isActive() {
		return active;
	}
}
