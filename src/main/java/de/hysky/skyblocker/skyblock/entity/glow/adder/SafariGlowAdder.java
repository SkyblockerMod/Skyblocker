package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HuntingConfig;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.hunting.safari.SafariUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.Entity;

public class SafariGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final SafariGlowAdder INSTANCE = new SafariGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		HuntingConfig huntingConfig = SkyblockerConfigManager.get().hunting;

		return switch (entity) {
			case ItemDisplay display when huntingConfig.hauntedBiome.highlightDuplico && SafariUtils.isInHauntedBiome() && display.getPosRotInterpolationDuration() == 3 -> huntingConfig.hauntedBiome.duplicoHighlightColor.getRGB();
			case Shulker shulker when huntingConfig.forestBiome.highlightHideonfloor && SafariUtils.isInForestBiome() && shulker.getColor() == DyeColor.GREEN -> huntingConfig.forestBiome.hideonfloorHighlightColor.getRGB();
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInSafari();
	}
}
