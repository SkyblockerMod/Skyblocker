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

    /**
     * Called when the player's Skyblock profile changes.
     * @implNote This is called upon receiving the chat message for the profile change rather than the exact moment of profile change, so it may be delayed by a few seconds.
     */
    public static final Event<ProfileChange> PROFILE_CHANGE = EventFactory.createArrayBacked(ProfileChange.class, callbacks -> (prev, profile) -> {
        for (ProfileChange callback : callbacks) {
            callback.onSkyblockProfileChange(prev, profile);
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

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface ProfileChange {
        void onSkyblockProfileChange(String prevProfileId, String profileId);
    }
}
