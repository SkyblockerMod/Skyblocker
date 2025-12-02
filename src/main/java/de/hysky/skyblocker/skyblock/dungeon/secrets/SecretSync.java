package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.utils.ws.Service;
import de.hysky.skyblocker.utils.ws.WsMessageHandler;
import de.hysky.skyblocker.utils.ws.message.*;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

public class SecretSync {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Logger LOGGER = LogUtils.getLogger();

	@Init
	public static void init() {
		DungeonEvents.ROOM_MATCHED.register(SecretSync::syncRoomMatch);
		DungeonEvents.SECRET_COUNT_UPDATED.register(SecretSync::syncSecretCount);
		DungeonEvents.SECRET_FOUND.register(SecretSync::syncSecretFound);
	}

	public static boolean checkUUID(UUID uuid, Message<?> msg) {
		if (CLIENT.world == null) return false;
		if (CLIENT.world.getPlayerByUuid(uuid) != null) return true;
		LOGGER.error("[Skyblocker Dungeon Secret Sync] Received a message from a player not in the Dungeons run, msg: {}", msg);
		return false;
	}

	@Nullable
	public static Room getRoomByName(String roomName) {
		return DungeonManager.getRoomsStream().filter(Room::isMatched).filter(rm -> rm.getName().equals(roomName)).findAny().orElse(null);
	}

	public static void syncRoomMatch(Room room) {
		if (CLIENT.player == null || room.fromWebsocket) return;
		List<Vector2ic> segments = room.getSegments().stream().toList();
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonRoomMatchMessage(CLIENT.player.getUuid(), room.getType(), room.getShape(), room.getDirection(), room.getName(), segments));
	}

	public static void handleRoomMatch(DungeonRoomMatchMessage msg) {
		if (!checkUUID(msg.uuid(), msg)) return;
		if (DungeonManager.getRoomMetadata(msg.room()) == null) {
			LOGGER.error("[Skyblocker Dungeons Secret Sync] Received an invalid room over the websocket, msg: {}", msg);
			return;
		}

		// just in case!
		if (DungeonManager.getRoomsStream().count() > 36) return;

		// Check if we already have this room
		if (!DungeonManager.validateRoomSegmentsFromWs(msg.pos())) return;

		// Make the room and add it
		Room newRoom = new Room(msg.roomType(), msg.shape(), msg.direction(), msg.room(), msg.pos().toArray(Vector2ic[]::new));
		DungeonManager.addRoomFromWs(newRoom);
	}

	/**
	 * Syncs the secret count, processed immediately.
	 */
	public static void syncSecretCount(Room room, boolean fromWS) {
		if (CLIENT.player == null || fromWS) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonRoomSecretCountMessage(CLIENT.player.getUuid(), room.getName(), room.getFoundSecretCount()));
	}

	public static void handleSecretCountUpdate(DungeonRoomSecretCountMessage msg) {
		if (!checkUUID(msg.uuid(), msg)) return;
		Room room = getRoomByName(msg.roomName());
		if (room == null || room.secretsFound >= msg.secretCount() || msg.secretCount() > room.getSecretCount()) return;

		room.secretsFound = msg.secretCount();
		room.secretCountOutdated = false;
		DungeonEvents.SECRET_COUNT_UPDATED.invoker().onSecretCountUpdate(room, true);
	}

	public static void syncSecretFound(Room room, SecretWaypoint waypoint) {
		if (CLIENT.player == null) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonRoomHideWaypointMessage(CLIENT.player.getUuid(), room.getName(), waypoint.hashCode()));
	}

	public static void handleHideWaypoint(DungeonRoomHideWaypointMessage msg) {
		if (!checkUUID(msg.uuid(), msg)) return;
		Room room = getRoomByName(msg.roomName());
		if (room == null) return;
		int index = room.getIndexByWaypointHash(msg.waypointHash());
		if (index == -1) return;
		room.markSecrets(index, true);
	}

	public static void syncMimicKilled() {
		if (CLIENT.player == null) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonMimicKilledMessage(CLIENT.player.getUuid()));
	}

	public static void handleMimicKilled(DungeonMimicKilledMessage msg) {
		if (!checkUUID(msg.uuid(), msg)) return;
		DungeonScore.onMimicKill();
	}

	public static void syncPrinceKilled() {
		if (CLIENT.player == null) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonPrinceKilledMessage(CLIENT.player.getUuid()));
	}

	public static void handlePrinceKilled(DungeonPrinceKilledMessage msg) {
		if (!checkUUID(msg.uuid(), msg)) return;
		DungeonScore.onPrinceKill(false);
	}
}
