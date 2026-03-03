package de.hysky.skyblocker.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class WorldEvents {
	/**
	 * Called upon a block's state being updated by the server.
	 *
	 * @implNote This event will not run when chunks are initially sent by the server and loaded by the client.
	 */
	public static final Event<BlockStateUpdate> BLOCK_STATE_UPDATE = EventFactory.createArrayBacked(BlockStateUpdate.class, callbacks -> (pos, oldState, newState) -> {
		for (BlockStateUpdate callback : callbacks) {
			callback.onBlockStateUpdate(pos, oldState, newState);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface BlockStateUpdate {
		/**
		 * @param pos The position of the block being updated. Note that there are no guarantees made of the mutability of this; if you are storing this
		 * somewhere it is highly recommended to make an immutable copy to avoid unintended results.
		 */
		void onBlockStateUpdate(BlockPos pos, @Nullable BlockState oldState, BlockState newState);
	}
}
