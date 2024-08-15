package de.hysky.skyblocker.utils.mayor;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a minister as retrieved from the API.
 * @param key The key of the minister.
 * @param name The name of the minister.
 * @param perk The perk of the minister.
 */
public record Minister(@NotNull String key, @NotNull String name, @NotNull Perk perk) {
	/**
	 * An empty minister. Allows for better null safety.
	 */
	public static final Minister EMPTY = new Minister("", "", Perk.EMPTY);

	/**
	 * For formatting purposes when printing out the result of the API call.
	 * @see MayorUtils#tickMayorCache()
	 */
	@Override
	public String toString() {
		if (isEmpty()) return "Mayor.EMPTY";
		return name;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}
}
