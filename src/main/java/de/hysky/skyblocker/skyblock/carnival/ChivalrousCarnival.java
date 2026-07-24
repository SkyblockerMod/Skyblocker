package de.hysky.skyblocker.skyblock.carnival;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;

public class ChivalrousCarnival {
	private static boolean isCarnivalActive = false;

	@Init
	public static void init() {
		SkyblockEvents.MAYOR_CHANGE.register(() ->
				isCarnivalActive = MayorUtils.getActivePerks().stream().anyMatch(perk -> perk.equals("Chivalrous Carnival"))
		);
	}

	public static boolean isCarnivalActive() {
		return isCarnivalActive;
	}

	protected static boolean isInCarnival() {
		return isCarnivalActive() && Utils.isOnSkyblock() && Utils.getLocation() == Location.HUB && Utils.getArea() == Area.Hub.CARNIVAL;
	}
}
