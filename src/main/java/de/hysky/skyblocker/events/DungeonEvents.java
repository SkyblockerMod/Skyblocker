package de.hysky.skyblocker.events;

import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DungeonEvents {
	/**
	 * Called when the player loads into a dungeon after the location is sent to the scoreboard.
	 */
	public static final Event<DungeonLoaded> DUNGEON_LOADED = EventFactory.createArrayBacked(DungeonLoaded.class, callbacks -> () -> {
		for (DungeonLoaded callback : callbacks) {
			callback.onDungeonLoaded();
		}
	});

	/**
	 * Called after the dungeons run starts and after the tab has changed to include additional information about the run such as each player's class.
	 */
	public static final Event<DungeonStarted> DUNGEON_STARTED = EventFactory.createArrayBacked(DungeonStarted.class, callbacks -> () -> {
		for (DungeonStarted callback : callbacks) {
			callback.onDungeonStarted();
		}
	});

	/**
	 * Called after the dungeons run ends and the team score is being sent in chat.
	 */
	public static final Event<DungeonEnded> DUNGEON_ENDED = EventFactory.createArrayBacked(DungeonEnded.class, callbacks -> () -> {
		for (DungeonEnded callback : callbacks) {
			callback.onDungeonEnded();
		}
	});

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

	public static final Event<SecretFound> SECRET_FOUND = EventFactory.createArrayBacked(SecretFound.class, callbacks -> (room, secretWaypoint) -> {
		for (SecretFound callback : callbacks) {
			callback.onSecretFound(room, secretWaypoint);
		}
	});

	public static final Event<SecretCountUpdate> SECRET_COUNT_UPDATED = EventFactory.createArrayBacked(SecretCountUpdate.class, callbacks -> (room, fromWS) -> {
		for (SecretCountUpdate callback : callbacks) {
			callback.onSecretCountUpdate(room, fromWS);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DungeonLoaded {
		void onDungeonLoaded();
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DungeonStarted {
		void onDungeonStarted();
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DungeonEnded {
		void onDungeonEnded();
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface RoomMatched {
		void onRoomMatched(Room room);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface SecretFound {
		void onSecretFound(Room room, SecretWaypoint secretWaypoint);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface SecretCountUpdate {
		void onSecretCountUpdate(Room room, boolean fromWS);
	}
}
