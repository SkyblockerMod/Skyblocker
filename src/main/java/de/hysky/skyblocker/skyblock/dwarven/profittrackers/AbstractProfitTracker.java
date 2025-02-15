package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import de.hysky.skyblocker.SkyblockerMod;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Abstract class for profit trackers that use the chat messages.
 * <br>
 * There isn't meant to be much inheritance from this class, it's more of a util class that provides some common methods.
 */
public abstract class AbstractProfitTracker {
	private static final String REWARD_TRACKERS_DIR = "reward-trackers";
	protected static final Pattern REWARD_PATTERN = Pattern.compile(" {4}(.*?) ?x?([\\d,]*)");
	protected static final Pattern HOTM_XP_PATTERN = Pattern.compile(" {4}\\+[\\d,]+ HOTM Experience");
	protected static final Pattern GEMSTONE_SYMBOLS = Pattern.compile("[α☘☠✎✧❁❂❈❤⸕] ");

	protected static String replaceGemstoneSymbols(String reward) {
		return GEMSTONE_SYMBOLS.matcher(reward).replaceAll("");
	}

	protected Path getRewardFilePath(String fileName) {
		return SkyblockerMod.CONFIG_DIR.resolve(REWARD_TRACKERS_DIR).resolve(fileName); // 2 resolve calls to avoid the need for a possibly confusing / placement
	}
}
