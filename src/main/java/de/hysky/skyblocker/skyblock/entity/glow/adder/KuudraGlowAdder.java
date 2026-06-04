package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.MagmaCube;

public class KuudraGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final KuudraGlowAdder INSTANCE = new KuudraGlowAdder();
	private static final int KUUDRA_COLOUR = 0xF7510F;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof MagmaCube magmaCube && SkyblockerConfigManager.get().crimsonIsle.kuudra.kuudraGlow && magmaCube.getSize() == Kuudra.KUUDRA_MAGMA_CUBE_SIZE ? KUUDRA_COLOUR : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInKuudra();
	}
}
