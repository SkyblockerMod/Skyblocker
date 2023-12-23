package de.hysky.skyblocker.events;

import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DungeonEvents {
    // TODO Some rooms such as creeper beam and water board does not get matched
    public static final Event<RoomMatched> PUZZLE_MATCHED = EventFactory.createArrayBacked(RoomMatched.class, callbacks -> room -> {
        for (RoomMatched callback : callbacks) {
            callback.onRoomMatched(room);
        }
    });

    public static final Event<RoomMatched> ROOM_MATCHED = EventFactory.createArrayBacked(RoomMatched.class, callbacks -> room -> {
        for (RoomMatched callback : callbacks) {
            callback.onRoomMatched(room);
        }
        if (room.getType() == Room.Type.PUZZLE) {
            PUZZLE_MATCHED.invoker().onRoomMatched(room);
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RoomMatched {
        void onRoomMatched(Room room);
    }
}
