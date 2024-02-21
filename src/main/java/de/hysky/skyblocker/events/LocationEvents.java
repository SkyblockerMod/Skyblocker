package de.hysky.skyblocker.events;

import de.hysky.skyblocker.utils.Location;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public class LocationEvents {
    public static final Event<LocationChange> CHANGE =
            EventFactory.createArrayBacked(LocationChange.class, callbacks -> location -> {
                for (LocationChange callback : callbacks)
                    callback.onLocationChange(location);
            });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface LocationChange {
        void onLocationChange(Location location);
    }
}
