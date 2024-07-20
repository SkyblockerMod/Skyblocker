package de.hysky.skyblocker.skyblock.crimson.kuudra;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.entity.mob.MagmaCubeEntity;

public class KuudraGlow {
	private static final int KUUDRA_SIZE = 30;

	public static boolean shouldGlow(MagmaCubeEntity magmaCube, String name) {
		return SkyblockerConfigManager.get().crimsonIsle.kuudra.kuudraGlow && magmaCube.getSize() == KUUDRA_SIZE && !name.equals("jeb_");
	}
}
