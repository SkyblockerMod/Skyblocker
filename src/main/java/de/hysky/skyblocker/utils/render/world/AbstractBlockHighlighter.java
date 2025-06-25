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
	protected final Block target;
	protected final float[] color;

	protected AbstractBlockHighlighter(Block target, DyeColor color) {
		this.target = target;
		this.color = ColorUtils.getFloatComponents(color);
	}

	protected void init() {
		ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
		ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
	}

	public void onBlockUpdate(BlockPos pos, BlockState state) {
		if (!shouldProcess()) return;

		if (shouldHighlight(state)) {
			highlightedBlocks.add(pos.toImmutable());
		} else {
			highlightedBlocks.remove(pos);
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
			highlightedBlocks.add(pos.toImmutable());
		});
	}

	/**
	 * Remove highlights in unloaded chunks.
	 */
	private void onChunkUnload(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;
		Iterator<BlockPos> iterator = highlightedBlocks.iterator();

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

		for (BlockPos highlight : highlightedBlocks) {
			renderBlock(highlight, context);
		}
	}

	protected void renderBlock(BlockPos pos, WorldRenderContext context) {
		RenderHelper.renderFilled(context, pos, color, 0.5f, false);
	}

	private void reset() {
		highlightedBlocks.clear();
	}

	protected abstract boolean shouldProcess();
}
