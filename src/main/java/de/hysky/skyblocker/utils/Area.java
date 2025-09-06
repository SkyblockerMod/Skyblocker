package de.hysky.skyblocker.utils;

import java.util.Arrays;

/**
 * An area of a Skyblock Island.
 */
public enum Area {
	CARNIVAL("Carnival"),
	CHATEAU("Stillgore Château"),
	FOREST("Forest"),
	FOREST_TEMPLE("Forest Temple"),
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
