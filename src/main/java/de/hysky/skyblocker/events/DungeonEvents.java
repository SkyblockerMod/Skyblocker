package de.hysky.skyblocker.events;

import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DungeonEvents {
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

	/**
	 * Note: This event fires after the tab has changed to include additional information about the run such as each player's class.
	 */
	public static final Event<DungeonStarted> DUNGEON_STARTED = EventFactory.createArrayBacked(DungeonStarted.class, callbacks -> () -> {
		for (DungeonStarted callback : callbacks) {
			callback.onDungeonStarted();
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface RoomMatched {
		void onRoomMatched(Room room);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DungeonStarted {
		void onDungeonStarted();
	}
}
