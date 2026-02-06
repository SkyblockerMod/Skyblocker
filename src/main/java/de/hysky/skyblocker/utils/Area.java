package de.hysky.skyblocker.utils;

import java.util.Arrays;

/**
 * An area of a Skyblock Island.
 */
public enum Area {
	// Private Island
	YOUR_ISLAND("Your Island"),
	// Hub
	BANK("Bank"),
	BAZAAR("Bazaar Alley"),
	CARNIVAL("Carnival"),
	// Farming Islands,
	GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave"),
	// Rift
	CHATEAU("Stillgore Château"),
	MIRRORVERSE("Mirrorverse"),
	// End
	THE_END("The End"),
	DRAGONS_NEST("Dragon's Nest"),
	// Foraging
	FOREST("Forest"),
	FOREST_TEMPLE("Forest Temple"),
	// Mining
	MINES_OF_DIVAN("Mines of Divan"),
	JUNGLE_TEMPLE("Jungle Temple"),
	DWARVEN_BASE_CAMP("Dwarven Base Camp"),
	GLACITE_TUNNELS("Glacite Tunnels"),
	GLACITE_MINESHAFTS("Glacite Mineshafts"),
	GREAT_GLACITE_LAKE("Great Glacite Lake"),
	// Crimson Isle
	DOJO("Dojo"),
	UNKNOWN("Unknown");

	private final String name;

	Area(String name) {
		this.name = name;
	}

	public static Area from(String name) {
		return Arrays.stream(values())
				.filter(area -> name.equals(area.name))
				.findFirst()
				.orElse(UNKNOWN);
	}
}
