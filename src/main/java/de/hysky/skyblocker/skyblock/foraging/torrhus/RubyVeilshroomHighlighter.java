package de.hysky.skyblocker.skyblock.foraging.torrhus;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.foraging.AbstractBlockHighlighter;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

public class RubyVeilshroomHighlighter extends AbstractBlockHighlighter {
	private static final RubyVeilshroomHighlighter INSTANCE = new RubyVeilshroomHighlighter();

	private RubyVeilshroomHighlighter() {
		super(Blocks.CRIMSON_FUNGUS, DyeColor.PURPLE);
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
	}

	@Override
	protected boolean shouldProcess() {
		return Utils.isInTorrhusCanyon() && SkyblockerConfigManager.get().foraging.torrhusCanyon.enableRubyVeilshroomHighlighter;
	}
}
