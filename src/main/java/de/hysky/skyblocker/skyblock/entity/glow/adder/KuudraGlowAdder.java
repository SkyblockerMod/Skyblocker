package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MagmaCubeEntity;

public class KuudraGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final KuudraGlowAdder INSTANCE = new KuudraGlowAdder();
	private static final int KUUDRA_COLOUR = 0xf7510f;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof MagmaCubeEntity magmaCube && SkyblockerConfigManager.get().crimsonIsle.kuudra.kuudraGlow && magmaCube.getSize() == Kuudra.KUUDRA_MAGMA_CUBE_SIZE ? KUUDRA_COLOUR : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInKuudra();
	}
}
