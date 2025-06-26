package de.hysky.skyblocker.utils.render.world;

import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Iterator;
import java.util.Set;

/**
 * Abstract class for a simple feature that highlights a certain type of block.
 */
public abstract class AbstractBlockHighlighter {
	private final Set<BlockPos> trackedBlocks = new ObjectOpenHashSet<>();
	protected final Block target;
	protected final float[] color;

	/**
	 * Convenience constructor for highlighting a specific block type.
	 *
	 * @param target Block to highlight.
	 * @param color Color to use for highlighting.
	 */
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

		if (state.isOf(target)) {
			trackedBlocks.add(pos.toImmutable());
		} else {
			trackedBlocks.remove(pos);
		}
	}

	/**
	 * Add initial highlights since {@link #onBlockUpdate(BlockPos, BlockState)} doesn't fire when the
	 * server sends chunk data via the {@code ChunkDataS2CPacket}.
	 */
	protected void onChunkLoad(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;

		chunk.forEachBlockMatchingPredicate(state -> state.isOf(target), (pos, state) -> {
			trackedBlocks.add(pos.toImmutable());
		});
	}

	/**
	 * Remove highlights in unloaded chunks.
	 */
	protected void onChunkUnload(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;
		Iterator<BlockPos> iterator = trackedBlocks.iterator();

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

		for (BlockPos highlight : trackedBlocks) {
			BlockState state = context.world().getBlockState(highlight);

			if (shouldRenderBlock(state)) {
				// Fill in the entire block, so the highlight is clearly visible even if the block is a cuboid (like a single sea pickle).
				RenderHelper.renderFilled(context, highlight, this.color, blockAlpha(state), false);
			}
		}
	}

	protected boolean shouldRenderBlock(BlockState state) {
		return true;
	}

	protected float blockAlpha(BlockState state) {
		return 0.5f;
	}

	public void reset() {
		trackedBlocks.clear();
	}

	/**
	 * @return Whether this highlighter should try to process blocks.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	protected abstract boolean shouldProcess();
}
