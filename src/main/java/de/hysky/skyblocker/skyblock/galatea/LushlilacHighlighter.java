package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;

public class LushlilacHighlighter extends AbstractBlockHighlighter {
	public static final LushlilacHighlighter INSTANCE = new LushlilacHighlighter(Blocks.FLOWERING_AZALEA, DyeColor.MAGENTA);

	private LushlilacHighlighter(Block target, DyeColor colour) {
		super(target, colour);
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
