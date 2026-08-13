package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;

public class GalateaGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final GalateaGlowAdder INSTANCE = new GalateaGlowAdder();



	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {

		return switch (entity) {
			case Shulker shulker when shulker.getColor() == DyeColor.GREEN && SkyblockerConfigManager.get().hunting.moongladeMobs.highlightHideonleaf -> SkyblockerConfigManager.get().hunting.moongladeMobs.hideonleafGlowColor.getRGB();
			case Turtle _ when SkyblockerConfigManager.get().hunting.moongladeMobs.highlightShellwise -> SkyblockerConfigManager.get().hunting.moongladeMobs.shellwiseGlowColor.getRGB();
			case Axolotl _ when SkyblockerConfigManager.get().hunting.moongladeMobs.highlightCoralot -> SkyblockerConfigManager.get().hunting.moongladeMobs.coralotGlowColor.getRGB();
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInGalatea();
	}
}
