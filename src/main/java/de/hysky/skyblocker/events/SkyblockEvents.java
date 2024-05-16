package de.hysky.skyblocker.events;

import de.hysky.skyblocker.utils.Location;
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

    public static final Event<SkyblockEvents.SkyblockLocationChange> LOCATION_CHANGE = EventFactory.createArrayBacked(SkyblockEvents.SkyblockLocationChange.class, callbacks -> location -> {
        for (SkyblockEvents.SkyblockLocationChange callback : callbacks) {
            callback.onSkyblockLocationChange(location);
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

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockLocationChange {
        void onSkyblockLocationChange(Location location);
    }
}
