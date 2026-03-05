package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.world.entity.Entity;

public class MushroomDesertGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final MushroomDesertGlowAdder INSTANCE = new MushroomDesertGlowAdder();
	private static final int KUUDRA_COLOUR = 0xF7510F;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		String name = MobGlow.getArmorStandName(entity);
		return name.contains("able") ? KUUDRA_COLOUR : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInFarm();
	}
}
