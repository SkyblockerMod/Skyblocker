package de.hysky.skyblocker.utils.mayor;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a mayor perk.
 * @param name The name of the perk.
 * @param description The description of the perk. This will include the formatting codes that are used in the game.
 */
public record Perk(@NotNull String name, @NotNull String description) {
	/**
	 * An empty perk.
	 * Represents a perk that does not exist for ministers.
	 * Allows for better null safety.
	 */
	public static final Perk EMPTY = new Perk("", "");
}
