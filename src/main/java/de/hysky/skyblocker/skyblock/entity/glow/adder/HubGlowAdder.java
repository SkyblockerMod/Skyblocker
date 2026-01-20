package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.carnival.CatchAFish;
import de.hysky.skyblocker.skyblock.carnival.ZombieShootout;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class HubGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final HubGlowAdder INSTANCE = new HubGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case Zombie zombie when ZombieShootout.isInZombieShootout() -> ZombieShootout.getZombieGlowColor(zombie);
			case ArmorStand armorStand when CatchAFish.isInCatchAFish() -> CatchAFish.getFishGlowColor(armorStand);
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInHub();
	}
}
