package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.preview.RoomPreviewServer;
import de.hysky.skyblocker.utils.ws.Service;
import de.hysky.skyblocker.utils.ws.WsMessageHandler;
import de.hysky.skyblocker.utils.ws.message.DungeonMimicKilledMessage;
import de.hysky.skyblocker.utils.ws.message.DungeonPrinceKilledMessage;
import de.hysky.skyblocker.utils.ws.message.DungeonRoomHideWaypointMessage;
import de.hysky.skyblocker.utils.ws.message.DungeonRoomMatchMessage;
import de.hysky.skyblocker.utils.ws.message.DungeonRoomSecretCountMessage;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public class SecretSync {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Supplier<DungeonsConfig.SecretSync> CONFIG = () -> SkyblockerConfigManager.get().dungeons.secretSync;

	@Init
	public static void init() {
		DungeonEvents.ROOM_MATCHED.register(SecretSync::syncRoomMatch);
		DungeonEvents.SECRET_COUNT_UPDATED.register(SecretSync::syncSecretCount);
		DungeonEvents.SECRET_FOUND.register(SecretSync::syncSecretFound);
	}

	public static boolean checkSender(UUID uuid) {
		if (Arrays.stream(DungeonPlayerManager.getPlayers()).filter(Objects::nonNull).anyMatch(player -> uuid.equals(player.uuid()))) {
			return true;
		}
		LOGGER.error("[Skyblocker Dungeon Secret Sync] Received a message from a player not in the Dungeons run: {}", uuid);
		return false;
	}

	public static @Nullable Room getRoomByName(String roomName) {
		return DungeonManager.getRoomsStream().filter(Room::isMatched).filter(rm -> rm.getName().equals(roomName)).findAny().orElse(null);
	}

	public static void syncRoomMatch(Room room) {
		if (CLIENT.player == null || room.fromWebsocket) return;
		List<Vector2ic> segments = room.getSegments().stream().toList();
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonRoomMatchMessage(CLIENT.player.getUUID(), room.getType(), room.getShape(), room.getDirection(), room.getName(), segments));
	}

	public static void handleRoomMatch(DungeonRoomMatchMessage msg) {
		if (!CONFIG.get().receiveRoomMatch || !checkSender(msg.sender())) return;
		if (DungeonManager.getRoomsStream().count() > 36 || msg.pos().size() > 4) return;
		if (DungeonManager.getRoomMetadata(msg.room()) == null) {
			LOGGER.error("[Skyblocker Dungeons Secret Sync] Received an invalid room over the websocket, msg: {}", msg);
			return;
		}
		if (DungeonManager.checkIfSegmentsExist(msg.pos())) return;

		// Validate shape
		Set<Vector2ic> segments = Set.of(msg.pos().toArray(Vector2ic[]::new));

		IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
		IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
		if (Room.determineShape(msg.roomType(), segments, segmentsX, segmentsY) != msg.shape()) {
			LOGGER.error("[Skyblocker Dungeons Secret Sync] Received a room with an invalid shape!, msg: {}", msg);
			return;
		}

		// Make the room and add it
		Room newRoom = new Room(msg.roomType(), msg.shape(), msg.direction(), msg.room(), segments, segmentsX, segmentsY);
		DungeonManager.addRoomFromWs(newRoom);
		LOGGER.info("[Skyblocker Dungeon Secret Sync] Added room {}", msg.room());
	}

	public static void syncSecretCount(Room room, boolean fromWS) {
		if (CLIENT.player == null || fromWS) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonRoomSecretCountMessage(CLIENT.player.getUUID(), room.getName(), room.getFoundSecretCount()));
	}

	public static void handleSecretCountUpdate(DungeonRoomSecretCountMessage msg) {
		if (!CONFIG.get().receiveRoomSecretCount || !checkSender(msg.sender())) return;
		Room room = getRoomByName(msg.roomName());
		if (room == null || room.secretsFound >= msg.secretCount() || msg.secretCount() > room.getSecretCount()) return;

		room.secretsFound = msg.secretCount();
		room.secretCountOutdated = false;
		DungeonEvents.SECRET_COUNT_UPDATED.invoker().onSecretCountUpdate(room, true);
	}

	public static void syncSecretFound(Room room, SecretWaypoint waypoint) {
		if (CLIENT.player == null || RoomPreviewServer.isActive) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonRoomHideWaypointMessage(CLIENT.player.getUUID(), room.getName(), waypoint.hashCode()));
	}

	public static void handleHideWaypoint(DungeonRoomHideWaypointMessage msg) {
		if (!checkSender(msg.sender()) || !CONFIG.get().hideReceivedWaypoints) return;
		Room room = getRoomByName(msg.roomName());
		if (room == null) return;
		int secretIndex = room.getIndexByWaypointHash(msg.waypointHash());
		if (secretIndex == -1) return;
		room.markSecrets(secretIndex, true);
		LOGGER.info("[Skyblocker Dungeon Secret Sync] Hiding waypoints for secret #{} in room {}", secretIndex, msg.roomName());
	}

	public static void syncMimicKilled() {
		if (CLIENT.player == null) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonMimicKilledMessage(CLIENT.player.getUUID()));
	}

	public static void handleMimicKilled(DungeonMimicKilledMessage msg) {
		if (!checkSender(msg.sender())) return;
		DungeonScore.onMimicKill();
		LOGGER.info("[Skyblocker Dungeon Secret Sync] Mimic killed!");
	}

	public static void syncPrinceKilled() {
		if (CLIENT.player == null) return;
		WsMessageHandler.sendServerMessage(Service.DUNGEON_SECRETS,
				new DungeonPrinceKilledMessage(CLIENT.player.getUUID()));
	}

	public static void handlePrinceKilled(DungeonPrinceKilledMessage msg) {
		if (!checkSender(msg.sender())) return;
		DungeonScore.onPrinceKill(false);
		LOGGER.info("[Skyblocker Dungeon Secret Sync] Prince killed!");
	}
}
