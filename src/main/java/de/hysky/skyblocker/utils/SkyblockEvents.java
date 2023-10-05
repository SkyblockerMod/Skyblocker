package de.hysky.skyblocker.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public final class SkyblockEvents {
    public static final Event<SkyblockEvents.SkyblockJoin> JOIN = EventFactory.createArrayBacked(SkyblockEvents.SkyblockJoin.class, callbacks -> () -> {
        for (SkyblockEvents.SkyblockJoin callback : callbacks) {
            callback.onSkyblockJoin();
        }
    });

    public static final Event<SkyblockEvents.SkyblockLeave> LEAVE = EventFactory.createArrayBacked(SkyblockEvents.SkyblockLeave.class, callbacks -> () -> {
        for (SkyblockEvents.SkyblockLeave callback : callbacks) {
            callback.onSkyblockLeave();
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockJoin {
        void onSkyblockJoin();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockLeave {
        void onSkyblockLeave();
    }
}
