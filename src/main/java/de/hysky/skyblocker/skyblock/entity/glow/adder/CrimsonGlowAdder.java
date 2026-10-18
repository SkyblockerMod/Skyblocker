package de.hysky.skyblocker.skyblock.entity.glow.adder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;

public class CrimsonGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final CrimsonGlowAdder INSTANCE = new CrimsonGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof Zombie zombie && DojoManager.inArena && DojoManager.shouldGlow(MobGlow.getArmorStandName(zombie)) ? DojoManager.getColor() : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInCrimson();
	}
}
