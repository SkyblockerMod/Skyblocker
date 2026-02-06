package de.hysky.skyblocker.skyblock.carnival;

import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;

public class ChivalrousCarnival {

	protected static boolean isInCarnival() {
		return Utils.isOnSkyblock() && Utils.getLocation() == Location.HUB && Utils.getArea() == Area.Hub.CARNIVAL;
	}
}
