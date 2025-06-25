package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.world.AbstractBlockHighlighter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public class SeaLumiesHighlighter extends AbstractBlockHighlighter {
	public static final SeaLumiesHighlighter INSTANCE = new SeaLumiesHighlighter(Blocks.SEA_PICKLE, DyeColor.CYAN);

	private SeaLumiesHighlighter(Block target, DyeColor colour) {
		super(target, colour);
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
	}

	@Override
	protected boolean shouldProcess() {
		return Utils.isInGalatea() && SkyblockerConfigManager.get().foraging.galatea.enableSeaLumiesHighlighter;
	}

	@Override
	protected void renderBlock(BlockPos pos, WorldRenderContext context) {
		BlockState state = context.world().getBlockState(pos);
		int pickles = state.get(SeaPickleBlock.PICKLES, 0);
		if (pickles > 0) {
			RenderHelper.renderFilled(context, pos, color, 0.2f * pickles, false);
		}
	}
}
