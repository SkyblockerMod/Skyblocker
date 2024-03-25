package de.hysky.skyblocker.events;

import de.hysky.skyblocker.utils.Location;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public class LocationEvents {
    public static final Event<LocationChange> CHANGE =
            EventFactory.createArrayBacked(LocationChange.class, callbacks -> newLocation -> {
                for (LocationChange callback : callbacks)
                    callback.onLocationChange(newLocation);
            });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface LocationChange {
        void onLocationChange(Location newLocation);
    }
}
