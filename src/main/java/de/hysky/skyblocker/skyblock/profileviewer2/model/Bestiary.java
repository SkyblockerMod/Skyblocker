package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Bestiary {
	/**
	 * Kill counts dived up by level: {@code [mob type]_[level]}
	 *
	 * @see PlayerData#kills
	 * @implNote The key type is Object because there is ONE entry that is not an int :(
	 */
	public NavigableMap<String, Object> kills = new TreeMap<>();

	/**
	 * Gets a submap of the map with only entries prefixed with the prefix. Assumes that the prefix is always followed by an {@code _} and no non underscore separated values exist.
	 */
	private static Map<String, Object> getPrefixMap(NavigableMap<String, Object> map, String prefix) {
		return map.subMap(prefix + "_", prefix + ("_" + 1));
	}

	// TODO do something about the Map<String, Object> later
	public Map<String, Object> getAllKills(String mobKind) {
		return getPrefixMap(this.kills, mobKind);
	}

	public Map<String, Object> getAllDeaths(String mobKind) {
		return getPrefixMap(this.deaths, mobKind);
	}

	/**
	 * Death counts dived up by level: {@code [mob type]_[level]}
	 */
	public NavigableMap<String, Object> deaths = new TreeMap<>();
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
