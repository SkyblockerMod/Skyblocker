package me.xmrvizzy.skyblocker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Fabric API currently doesn't have an event for this
public class ClientPlayerBlockBreakEvent {
    public static final Event<AfterBlockBreak> AFTER = EventFactory.createArrayBacked(AfterBlockBreak.class,
            (listeners) -> (world, player, pos, state) -> {
                for (AfterBlockBreak listener : listeners) {
                    listener.afterBlockBreak(world, player, pos, state);
                }
            });

    @FunctionalInterface
    public interface AfterBlockBreak {
        void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state);
    }
}
