package de.hysky.skyblocker.utils.render.world;

import java.util.*;

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
public abstract class AbstractBlockHighlighter {
	private final Set<BlockPos> highlightedBlocks = new HashSet<>();
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

		if (shouldHighlight(state)) {
			this.highlightedBlocks.add(pos.toImmutable());
		} else {
			this.highlightedBlocks.remove(pos);
		}
	}

	protected boolean shouldHighlight(BlockState state) {
		return state.isOf(target);
	}

	/**
	 * Add initial highlights since {@link #onBlockUpdate(BlockPos, BlockState)} doesn't fire when the
	 * server sends chunk data via the {@code ChunkDataS2CPacket}.
	 */
	private void onChunkLoad(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;

		chunk.forEachBlockMatchingPredicate(this::shouldHighlight, (pos, state) -> {
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
			renderBlock(highlight, context);
		}
	}

	protected void renderBlock(BlockPos pos, WorldRenderContext context) {
		RenderHelper.renderFilled(context, pos, this.colour, 0.5f, false);
	}

	private void reset() {
		this.highlightedBlocks.clear();
	}

	protected abstract boolean shouldProcess();
}
