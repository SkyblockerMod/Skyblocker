package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record DungeonRoomHideWaypointMessage(String type, UUID uuid, String roomName, int waypointHash) implements Message<DungeonRoomHideWaypointMessage> {
	public static final String TYPE = "room_hide_waypoint";
	public static final Codec<DungeonRoomHideWaypointMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonRoomHideWaypointMessage::type),
					Uuids.STRING_CODEC.fieldOf("uuid").forGetter(DungeonRoomHideWaypointMessage::uuid),
					Codec.STRING.fieldOf("room").forGetter(DungeonRoomHideWaypointMessage::roomName),
					Codec.INT.fieldOf("waypointHash").forGetter(DungeonRoomHideWaypointMessage::waypointHash))
			.apply(instance, DungeonRoomHideWaypointMessage::new));

	public DungeonRoomHideWaypointMessage(UUID uuid, String roomName, int waypointHash) {
		this(TYPE, uuid, roomName, waypointHash);
	}

	@Override
	public Codec<DungeonRoomHideWaypointMessage> getCodec() {
		return CODEC;
	}
}
