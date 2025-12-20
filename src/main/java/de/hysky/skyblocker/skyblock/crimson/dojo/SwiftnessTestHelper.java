package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwiftnessTestHelper {

	private static BlockPos lastBlock;

	protected static void reset() {
		lastBlock = null;
	}

	protected static void onBlockUpdate(BlockPos pos, BlockState state) {
		if (state.is(Blocks.LIME_WOOL)) {
			lastBlock = pos.immutable();
		}
	}

	/**
	 * Renders a green block around the newest block
	 */
	protected static void extractRendering(PrimitiveCollector collector) {
		if (lastBlock == null) {
			return;
		}
		collector.submitFilledBox(lastBlock, new float[]{0f, 1f, 0f}, 0.5f, true);
	}
}
