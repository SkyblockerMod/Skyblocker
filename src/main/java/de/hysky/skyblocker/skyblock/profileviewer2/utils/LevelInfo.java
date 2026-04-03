package de.hysky.skyblocker.skyblock.profileviewer2.utils;

// TODO more info: xpForNextLevel, xpToNextLevel, match old level finder equivalent
public record LevelInfo(long xp, int level, long xpForNextLevel, long xpTowardsNextLevel) {

	public double percentageToNextLevel() {
		return this.xpTowardsNextLevel() > 0 && this.xpForNextLevel() > 0 ? (double) this.xpTowardsNextLevel() / (double) this.xpForNextLevel() : 1f;
	}
}
