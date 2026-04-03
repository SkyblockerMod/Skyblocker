package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.equine.Horse;

public class MushroomDesertGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final MushroomDesertGlowAdder INSTANCE = new MushroomDesertGlowAdder();

	private static final int TRACKABLE_COLOR = 0xFFFFFF;
	private static final int UNTRACKABLE_COLOR = 0x55FF55;
	private static final int UNDETECTED_COLOR = 0x5555FF;
	private static final int ENDANGERED_COLOR = 0xAA00AA;
	private static final int ELUSIVE_COLOR = 0xFFAA00;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		if (!SkyblockerConfigManager.get().otherLocations.barn.enablePeltAnimalHighlighter) {
			return NO_GLOW;
		}

		String name = MobGlow.getArmorStandName(entity);

		return switch (entity) {
			case Cow _ when isPeltAnimal(name, "Cow") -> getGlowColor(name);
			case Pig _ when isPeltAnimal(name, "Pig") -> getGlowColor(name);
			case Sheep _ when isPeltAnimal(name, "Sheep") -> getGlowColor(name);
			case Rabbit _ when isPeltAnimal(name, "Rabbit") -> getGlowColor(name);
			case Chicken _ when isPeltAnimal(name, "Chicken") -> getGlowColor(name);
			case Horse _ when isPeltAnimal(name, "Horse") -> getGlowColor(name);
			default -> NO_GLOW;
		};
	}

	// Checks if the name follows the pelt animal naming convention
	private static boolean isPeltAnimal(String name, String animal) {
		return name.contains("Trackable " + animal)
				|| name.contains("Untrackable " + animal)
				|| name.contains("Undetected " + animal)
				|| name.contains("Endangered " + animal)
				|| name.contains("Elusive " + animal);
	}

	private int getGlowColor(String name) {
		if (name.contains("Trackable")) return TRACKABLE_COLOR;
		if (name.contains("Untrackable")) return UNTRACKABLE_COLOR;
		if (name.contains("Undetected")) return UNDETECTED_COLOR;
		if (name.contains("Endangered")) return ENDANGERED_COLOR;
		if (name.contains("Elusive")) return ELUSIVE_COLOR;

		return NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInFarm();
	}
}
