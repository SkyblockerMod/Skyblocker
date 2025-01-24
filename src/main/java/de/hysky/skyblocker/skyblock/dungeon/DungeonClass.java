package de.hysky.skyblocker.skyblock.dungeon;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.hysky.skyblocker.skyblock.entity.MobGlow;

public enum DungeonClass {
	UNKNOWN("Unknown", MobGlow.NO_GLOW),
	HEALER("Healer", 0x820dd1),
	MAGE("Mage", 0x36c6e3),
	BERSERK("Berserk", 0xfa5b16),
	ARCHER("Archer", 0xed240e),
	TANK("Tank", 0x138717);

	private static final Map<String, DungeonClass> CLASSES = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(DungeonClass::displayName, Function.identity()));

	private final String name;
	private final int color;

	DungeonClass(String name, int colour) {
		this.name = name;
		this.color = colour;
	}

	public String displayName() {
		return this.name;
	}

	/**
	 * @return The color of the class in RGB format.
	 */
	public int color() {
		return this.color;
	}

	public static DungeonClass from(String name) {
		return CLASSES.getOrDefault(name, UNKNOWN);
	}
}
