package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.Utils;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

public class SeaLumiesHighlighter extends AbstractBlockHighlighter {
	private final BlockPosSet allBlocks = new BlockPosSet();

	@Override
	public void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState) {
		if (!shouldProcess()) return;

		if (this.statePredicate.test(newState)) {
			this.allBlocks.add(pos);
			if (isEnabled() && isEnoughPickles(newState)) this.highlightedBlocks.add(pos);
		} else {
			this.allBlocks.remove(pos);
			this.highlightedBlocks.remove(pos);
		}
	}

	public static final SeaLumiesHighlighter INSTANCE = new SeaLumiesHighlighter();

	private SeaLumiesHighlighter() {
		super(Blocks.SEA_PICKLE, DyeColor.CYAN);
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
	}

	@Override
	protected void onChunkUnload(ClientLevel world, LevelChunk chunk) {
		if (!shouldProcess()) return;
		Iterator<BlockPos> iterator = this.allBlocks.iterator();
		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			ChunkAccess holder = world.getChunk(pos);

			if (holder.equals(chunk)) {
				iterator.remove();
				highlightedBlocks.remove(pos);
			}
		}
	}

	@Override
	protected void onChunkLoad(ClientLevel world, LevelChunk chunk) {
		if (!shouldProcess()) return;

		chunk.findBlocks(statePredicate, (pos, state) -> {
			this.allBlocks.add(pos);
			if (isEnabled() && isEnoughPickles(state)) this.highlightedBlocks.add(pos);
		});
	}

	@Override
	protected boolean shouldProcess() {
		return Utils.isInGalatea(); // Process all blocks, just don't highlight them if the config is disabled. This way, changing the config has an immediate effect rather than waiting for a chunk reload.
	}

	@Override
	public void reset() {
		this.allBlocks.clear();
		this.highlightedBlocks.clear();
	}

	// Called when either the min count or the enabled state changes.
	public void configCallback() {
		this.highlightedBlocks.clear();
		ClientLevel world = Minecraft.getInstance().level;
		if (!shouldProcess() || world == null || !isEnabled()) {
			return;
		}

		for (BlockPos pos : this.allBlocks) {
			BlockState state = world.getBlockState(pos);
			if (this.statePredicate.test(state) && isEnoughPickles(state)) {
				this.highlightedBlocks.add(pos);
			}
		}
	}

	private boolean isEnoughPickles(BlockState state) {
		return state.hasProperty(SeaPickleBlock.PICKLES) && state.getValue(SeaPickleBlock.PICKLES) >= SkyblockerConfigManager.get().foraging.galatea.seaLumiesMinimumCount;
	}

	private boolean isEnabled() {
		return SkyblockerConfigManager.get().foraging.galatea.enableSeaLumiesHighlighter;
	}
}
