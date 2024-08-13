package de.hysky.skyblocker.utils.mayor;

/**
 * Represents a mayor perk.
 * @param name The name of the perk.
 * @param description The description of the perk. This will include the formatting codes that are used in the game.
 */
public record Perk(String name, String description) {
	/**
	 * An empty perk. Allows for better null safety.
	 */
	public static Perk EMPTY = new Perk("", "");
}
