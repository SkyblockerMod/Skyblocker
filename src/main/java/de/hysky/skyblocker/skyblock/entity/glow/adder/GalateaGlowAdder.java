package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;

public class GalateaGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final GalateaGlowAdder INSTANCE = new GalateaGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case ShulkerEntity shulker when shulker.getColor() == DyeColor.GREEN && SkyblockerConfigManager.get().hunting.huntingMobs.highlightHideonleaf -> DyeColor.YELLOW.getSignColor();

			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInGalatea();
	}
}
