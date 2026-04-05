package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

/**
 * The default catacombs info contains some extra stats that are shared across master mode and normal mode.
 * I am however not entirely sure everything in here is fully shared between the two modes.
 */
public class DefaultCatacombs extends GenericCatacombs {
	public double experience;
	/**
	 * Aggregate attempts (including failures) across master mode and regular mode.
	 */
	@SerializedName("times_played")
	public AggregateStat timesPlayed;
	/**
	 * Aggregate watcher skills across master mode and regular mode.
	 */
	@SerializedName("watcher_kills")
	public AggregateStat watcherKills;
}
