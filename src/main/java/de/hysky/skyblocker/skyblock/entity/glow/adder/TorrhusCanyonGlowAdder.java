package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;

public class TorrhusCanyonGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final TorrhusCanyonGlowAdder INSTANCE = new TorrhusCanyonGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case Shulker shulker when (shulker.getColor() == DyeColor.YELLOW || shulker.getColor() == DyeColor.ORANGE || shulker.getColor() == DyeColor.BROWN) && SkyblockerConfigManager.get().hunting.torrhusMobs.highlightHideonsun -> SkyblockerConfigManager.get().hunting.torrhusMobs.hideonsunHighlightColor.getRGB();
			default -> MobGlow.NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInTorrhusCanyon();
	}
}
