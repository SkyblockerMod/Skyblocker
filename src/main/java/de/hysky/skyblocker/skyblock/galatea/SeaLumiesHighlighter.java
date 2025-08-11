package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Iterator;
import java.util.Set;

public class SeaLumiesHighlighter extends AbstractBlockHighlighter {
	private final Set<BlockPos> allBlocks = new ObjectOpenHashSet<>();

	@Override
	public void onBlockUpdate(BlockPos pos, BlockState state) {
		if (!shouldProcess()) return;

		if (this.statePredicate.test(state)) {
			this.allBlocks.add(pos.toImmutable());
			if (isEnoughPickles(state)) this.highlightedBlocks.add(pos.toImmutable());
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
	protected void onChunkUnload(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;
		Iterator<BlockPos> iterator = this.allBlocks.iterator();
		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			Chunk holder = world.getChunk(pos);

			if (holder.equals(chunk)) {
				iterator.remove();
				highlightedBlocks.remove(pos);
			}
		}
	}

	@Override
	protected void onChunkLoad(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;

		chunk.forEachBlockMatchingPredicate(statePredicate, (pos, state) -> {
			this.allBlocks.add(pos.toImmutable());
			if (isEnoughPickles(state)) this.highlightedBlocks.add(pos.toImmutable());
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
		ClientWorld world = MinecraftClient.getInstance().world;
		if (!shouldProcess() || world == null || !SkyblockerConfigManager.get().foraging.galatea.enableSeaLumiesHighlighter) {
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
		return state.contains(SeaPickleBlock.PICKLES) && state.get(SeaPickleBlock.PICKLES) >= SkyblockerConfigManager.get().foraging.galatea.seaLumiesMinimumCount;
	}
}
