package de.hysky.skyblocker.skyblock.hunting.safari;

import de.hysky.skyblocker.utils.SkyBlockBiomes;
import de.hysky.skyblocker.utils.Utils;

public class SafariUtils {

	public static boolean isInHauntedBiome() {
		return Utils.isInSafari() && Utils.isInBiome(SkyBlockBiomes.HAUNTED);
	}

	public static boolean isInForestBiome() {
		return Utils.isInSafari() && Utils.isInBiome(SkyBlockBiomes.FOREST);
	}

	public static boolean isInCavernBiome() {
		return Utils.isInSafari() && Utils.isInBiome(SkyBlockBiomes.CAVERN);
	}

	public static boolean isInIcyBiome() {
		return Utils.isInSafari() && (Utils.isInBiome(SkyBlockBiomes.ICY) || Utils.isInBiome(SkyBlockBiomes.ICY_CAVES));
	}
}
