package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Abstract class for a simple feature that highlights a certain type of block.
 */
// TODO Move this to a more generic package since this is not foraging specific (maybe make a world rendering utility package?)
public abstract class AbstractBlockHighlighter {
	protected final BlockPosSet highlightedBlocks = new BlockPosSet();
	protected final float[] colour;
	protected final BiPredicate<ClientLevel, BlockPos> posPredicate;
	protected final Predicate<BlockState> statePredicate;

	/**
	 * Convenience constructor for highlighting a specific block type.
	 *
	 * @param target Block to highlight.
	 * @param colour colour to use for highlighting.
	 */
	protected AbstractBlockHighlighter(Block target, DyeColor colour) {
		this(state -> state.is(target), colour);
	}

	/**
	 * @param statePredicate predicate that the block state must match to be highlighted.
	 * @param colour colour to use for highlighting.
	 */
	protected AbstractBlockHighlighter(Predicate<BlockState> statePredicate, DyeColor colour) {
		this((_, _) -> true, statePredicate, ColorUtils.getFloatComponents(colour));
	}

	/**
	 * @param posPredicate predicate that the block position must match to be highlighted.
	 * @param statePredicate predicate that the block state must match to be highlighted.
	 * @param colour colour to use for highlighting.
	 */
	protected AbstractBlockHighlighter(BiPredicate<ClientLevel, BlockPos> posPredicate, Predicate<BlockState> statePredicate, float[] colour) {
		this.posPredicate = posPredicate;
		this.statePredicate = statePredicate;
		this.colour = colour;
	}

	protected void init() {
		ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
		ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
		LevelRenderExtractionCallback.EVENT.register(this::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> this.reset());
		WorldEvents.BLOCK_STATE_UPDATE.register(this::onBlockUpdate);
	}

	protected void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState) {
		if (!shouldProcess()) return;

		if (this.posPredicate.test(Objects.requireNonNull(Minecraft.getInstance().level), pos) && this.statePredicate.test(newState)) {
			this.highlightedBlocks.add(pos);
		} else {
			this.highlightedBlocks.remove(pos);
		}
	}

	/**
	 * Add initial highlights since {@link #onBlockUpdate(BlockPos, BlockState)} doesn't fire when the
	 * server sends chunk data via the {@code ChunkDataS2CPacket}.
	 */
	protected void onChunkLoad(ClientLevel level, LevelChunk chunk) {
		if (!shouldProcess()) return;

		chunk.findBlocks(this.statePredicate, (pos, _) -> {
			if (this.posPredicate.test(level, pos)) {
				this.highlightedBlocks.add(pos);
			}
		});
	}

	/**
	 * Remove highlights in unloaded chunks.
	 */
	protected void onChunkUnload(ClientLevel world, LevelChunk chunk) {
		if (!shouldProcess()) return;
		Iterator<BlockPos> iterator = this.highlightedBlocks.iterator();

		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			ChunkAccess holder = world.getChunk(pos);

			if (holder.equals(chunk)) {
				iterator.remove();
			}
		}
	}

	private void extractRendering(PrimitiveCollector collector) {
		Minecraft client = Minecraft.getInstance();
		if (!shouldProcess() || client.level == null) return;

		for (BlockPos highlight : this.highlightedBlocks.iterateMut()) {
			AABB outline = RenderHelper.getBlockBoundingBox(client.level, highlight);

			if (outline != null) {
				collector.submitFilledBox(outline, this.colour, 0.4f, false);
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
