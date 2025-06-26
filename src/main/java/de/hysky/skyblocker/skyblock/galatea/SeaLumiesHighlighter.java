package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.world.AbstractBlockHighlighter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public class SeaLumiesHighlighter extends AbstractBlockHighlighter {
	public static final SeaLumiesHighlighter INSTANCE = new SeaLumiesHighlighter();

	private SeaLumiesHighlighter() {
		super(Blocks.SEA_PICKLE, DyeColor.CYAN);
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
	}

	@Override
	protected boolean shouldProcess() {
		// Process all blocks, just don't highlight them if the config is disabled.
		// This way, changing the config has an immediate effect rather than waiting for a chunk reload.
		return Utils.isInGalatea();
	}

	@Override
	protected boolean shouldRenderBlock(BlockState state) {
		return SkyblockerConfigManager.get().foraging.galatea.enableSeaLumiesHighlighter &&
				state.contains(SeaPickleBlock.PICKLES) &&
				state.get(SeaPickleBlock.PICKLES) >= SkyblockerConfigManager.get().foraging.galatea.seaLumiesMinimumCount;
	}

	@Override
	protected float blockAlpha(BlockState state) {
		return state.get(SeaPickleBlock.PICKLES, 0) * 0.2f;
	}
}
