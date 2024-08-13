package de.hysky.skyblocker.utils.mayor;

import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.util.List;

/**
 * Represents a mayor as retrieved from the API.
 * @param key The key of the mayor.
 * @param name The name of the mayor.
 * @param perks The perks of the mayor.
 */
public record Mayor(String key, String name, List<Perk> perks) {
	/**
	 * An empty mayor. Allows for better null safety.
	 */
	public static final Mayor EMPTY = new Mayor("", "", ObjectLists.emptyList());
}
