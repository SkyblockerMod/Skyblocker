package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;

public class CrimsonGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final CrimsonGlowAdder INSTANCE = new CrimsonGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof ZombieEntity zombie && DojoManager.inArena && DojoManager.shouldGlow(MobGlow.getArmorStandName(zombie)) ? DojoManager.getColor() : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInCrimson();
	}
}
