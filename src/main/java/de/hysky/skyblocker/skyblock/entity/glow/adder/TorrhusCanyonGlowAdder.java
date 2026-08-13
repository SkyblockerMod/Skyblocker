package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.parrot.Parrot;
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
			case Shulker shulker when SkyblockerConfigManager.get().hunting.torrhusMobs.highlightHideonsun && (shulker.getColor() == DyeColor.YELLOW || shulker.getColor() == DyeColor.ORANGE || shulker.getColor() == DyeColor.BROWN) -> SkyblockerConfigManager.get().hunting.torrhusMobs.hideonsunHighlightColor.getRGB();
			case Parrot parrot when SkyblockerConfigManager.get().hunting.torrhusMobs.highlightBlueJay && parrot.getVariant() == Parrot.Variant.BLUE -> SkyblockerConfigManager.get().hunting.torrhusMobs.blueJayHighlightColor.getRGB();
			default -> MobGlow.NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInTorrhusCanyon();
	}
}
