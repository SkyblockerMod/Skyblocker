package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.carnival.CatchAFish;
import de.hysky.skyblocker.skyblock.carnival.ZombieShootout;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.ZombieEntity;

public class HubGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final HubGlowAdder INSTANCE = new HubGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case ZombieEntity zombie when ZombieShootout.isInZombieShootout() -> ZombieShootout.getZombieGlowColor(zombie);
			case ArmorStandEntity armorStand when CatchAFish.isInCatchAFish() -> CatchAFish.getFishGlowColor(armorStand);
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInHub();
	}
}
