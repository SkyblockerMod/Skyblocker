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
			case Shulker shulker when shulker.getColor() == DyeColor.GREEN && SkyblockerConfigManager.get().hunting.huntingMobs.highlightHideonleaf -> SkyblockerConfigManager.get().hunting.huntingMobs.hideonleafGlowColor.getRGB();
			case Turtle turtle when SkyblockerConfigManager.get().hunting.huntingMobs.highlightShellwise -> SkyblockerConfigManager.get().hunting.huntingMobs.shellwiseGlowColor.getRGB();
			case Axolotl ax when SkyblockerConfigManager.get().hunting.huntingMobs.highlightCoralot -> SkyblockerConfigManager.get().hunting.huntingMobs.coralotGlowColor.getRGB();
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInGalatea();
	}
}
