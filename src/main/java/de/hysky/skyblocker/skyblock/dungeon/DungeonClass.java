package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DungeonClass {
	UNKNOWN("Unknown", MobGlow.NO_GLOW, Ico.BARRIER),
	HEALER("Healer", 0x820dd1, Ico.POTION),
	MAGE("Mage", 0x36c6e3, Ico.B_ROD),
	BERSERK("Berserk", 0xfa5b16, Ico.DIA_SWORD),
	ARCHER("Archer", 0xed240e, Ico.BOW),
	TANK("Tank", 0x138717, Ico.CHESTPLATE);

	private static final Map<String, DungeonClass> CLASSES = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(DungeonClass::displayName, Function.identity()));

	private final String name;
	private final int color;
	private final ItemStack icon;

	DungeonClass(String name, int color, ItemStack icon) {
		this.name = name;
		this.color = color;
		this.icon = icon;
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

	public ItemStack icon() {
		return icon;
	}

	public static DungeonClass from(String name) {
		return CLASSES.getOrDefault(name, UNKNOWN);
	}
}
