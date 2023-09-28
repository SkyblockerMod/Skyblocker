package me.xmrvizzy.skyblocker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

// Fabric API currently doesn't have an event for this
public class ClientPlayerBlockBreakEvent {
    public static final Event<AfterBlockBreak> AFTER = EventFactory.createArrayBacked(AfterBlockBreak.class,
        (listeners) -> (pos, player) -> {
            for (AfterBlockBreak listener : listeners) {
                listener.afterBlockBreak(pos, player);
            }
        });

    @FunctionalInterface
    public interface AfterBlockBreak {
        void afterBlockBreak(BlockPos pos, PlayerEntity player);
    }
}
