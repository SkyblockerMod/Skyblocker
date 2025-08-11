package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Abstract class for a simple feature that highlights a certain type of block.
 */
//TODO Move this to a more generic package since this is not Galatea specific (maybe make a world rendering utility package?)
public abstract class AbstractBlockHighlighter {
	protected final Set<BlockPos> highlightedBlocks = new ObjectOpenHashSet<>();
	protected final float[] colour;
	protected final Predicate<BlockState> statePredicate;

	/**
	 * Convenience constructor for highlighting a specific block type.
	 *
	 * @param target Block to highlight.
	 * @param colour Color to use for highlighting.
	 */
	protected AbstractBlockHighlighter(Block target, DyeColor colour) {
		this(state -> state.isOf(target), colour);
	}

	/**
	 * @param statePredicate Predicate that the blockstate must match to be highlighted.
	 * @param colour Color to use for highlighting.
	 */
	protected AbstractBlockHighlighter(Predicate<BlockState> statePredicate, DyeColor colour) {
		this.statePredicate = statePredicate;
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

		if (this.statePredicate.test(state)) {
			this.highlightedBlocks.add(pos.toImmutable());
		} else {
			this.highlightedBlocks.remove(pos);
		}
	}

	/**
	 * Add initial highlights since {@link #onBlockUpdate(BlockPos, BlockState)} doesn't fire when the
	 * server sends chunk data via the {@code ChunkDataS2CPacket}.
	 */
	protected void onChunkLoad(ClientWorld world, WorldChunk chunk) {
		if (!shouldProcess()) return;

		chunk.forEachBlockMatchingPredicate(statePredicate, (pos, state) -> this.highlightedBlocks.add(pos.toImmutable()));
	}

	/**
	 * Remove highlights in unloaded chunks.
	 */
	protected void onChunkUnload(ClientWorld world, WorldChunk chunk) {
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
		MinecraftClient client = MinecraftClient.getInstance();
		if (!shouldProcess() || client.world == null) return;

		for (BlockPos highlight : this.highlightedBlocks) {
			Box outline = RenderHelper.getBlockBoundingBox(client.world, highlight);

			if (outline != null) {
				RenderHelper.renderFilled(context, outline, this.colour, 0.4f, false);
			}
		}
	}

	public void reset() {
		this.highlightedBlocks.clear();
	}

	/**
	 * @return Whether this highlighter should try to process blocks.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	protected abstract boolean shouldProcess();
}
