package de.hysky.skyblocker.skyblock.galatea;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Abstract class for a simple feature that highlights a certain type of block.
 */
//TODO Move this to a more generic package since this is not Galatea specific (maybe make a world rendering utility package?)
public abstract class AbstractBlockHighlighter {
	private final List<BlockPos> highlightedBlocks = new ArrayList<>();
	private final Block target;
	private final float[] colour;

	protected AbstractBlockHighlighter(Block target, DyeColor colour) {
		this.target = target;
		this.colour = ColorUtils.getFloatComponents(colour);
	}

	protected void init() {
		ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
		ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> this.reset());
	}

	public void onBlockUpdate(BlockPos pos, BlockState state) {
		if (!shouldProcess()) return;

		if (state.getBlock().equals(this.target)) {
			this.highlightedBlocks.add(pos.toImmutable());
		} else {
			this.highlightedBlocks.remove(pos);
		}
	}

	/**
	 * Add initial highlights since {@link #onBlockUpdate(BlockPos, BlockState)} doesn't fire when the
	 * server sends chunk data via the {@code ChunkDataS2CPacket}.
	 */
	private void onChunkLoad(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;

		chunk.forEachBlockMatchingPredicate(state -> state.getBlock().equals(this.target), (pos, state) -> {
			this.highlightedBlocks.add(pos.toImmutable());
		});
	}

	/**
	 * Remove highlights in unloaded chunks.
	 */
	private void onChunkUnload(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;
		Iterator<BlockPos> iterator = this.highlightedBlocks.iterator();

		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			Chunk holder = world.getChunk(pos);

			if (holder.equals(chunk)) {
				iterator.remove();
			}
		}
	}

	private void render(WorldRenderContext context) {
		if (!shouldProcess()) return;

		for (BlockPos highlight : this.highlightedBlocks) {
			RenderHelper.renderFilled(context, highlight, this.colour, 0.5f, false);
		}
	}

	private void reset() {
		this.highlightedBlocks.clear();
	}

	protected abstract boolean shouldProcess();
}
