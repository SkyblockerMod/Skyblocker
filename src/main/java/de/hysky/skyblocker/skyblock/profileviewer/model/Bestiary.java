package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Bestiary {
	/**
	 * Kill counts dived up by level: {@code [mob type]_[level]}
	 *
	 * @see PlayerData#kills
	 */
	public NavigableMap<String, Integer> kills = new TreeMap<>();

	/**
	 * Gets a submap of the map with only entries prefixed with the prefix. Assumes that the prefix is always followed by an {@code _} and no non underscore separated values exist.
	 */
	private static Map<String, Integer> getPrefixMap(NavigableMap<String, Integer> map, String prefix) {
		return map.subMap(prefix + '_', prefix + ('_' + 1));
	}

	public Map<String, Integer> getAllKills(String mobKind) {
		return getPrefixMap(kills, mobKind);
	}

	public Map<String, Integer> getAllDeaths(String mobKind) {
		return getPrefixMap(deaths, mobKind);
	}

	/**
	 * Death counts dived up by level: {@code [mob type]_[level]}
	 */
	public NavigableMap<String, Integer> deaths = new TreeMap<>();
	/**
	 * There is an old format. This class really deserves to be behind an error boundary for those cases.
	 */
	public boolean migrated_stats = true;
	public Milestone milestone = new Milestone();

	public static class Milestone {
		@SerializedName("last_claimed_milestone")
		public int lastClaimedMilestone;
	}
}
