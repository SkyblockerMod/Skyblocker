package de.hysky.skyblocker.skyblock.foraging.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.foraging.AbstractBlockHighlighter;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

public final class LushlilacHighlighter extends AbstractBlockHighlighter {
	private static final LushlilacHighlighter INSTANCE = new LushlilacHighlighter();

	private LushlilacHighlighter() {
		super(Blocks.FLOWERING_AZALEA, DyeColor.MAGENTA);
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
	}

	@Override
	protected boolean shouldProcess() {
		return Utils.isInGalatea() && SkyblockerConfigManager.get().foraging.galatea.enableLushlilacHighlighter;
	}
}
