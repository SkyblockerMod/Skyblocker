package de.hysky.skyblocker.utils.mayor;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a mayor as retrieved from the API.
 * @param key The key of the mayor.
 * @param name The name of the mayor.
 * @param perks The perks of the mayor.
 */
public record Mayor(@NotNull String key, @NotNull String name, @NotNull List<Perk> perks) {
	/**
	 * An empty mayor. Allows for better null safety.
	 */
	public static final Mayor EMPTY = new Mayor("", "", ObjectLists.emptyList());

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
