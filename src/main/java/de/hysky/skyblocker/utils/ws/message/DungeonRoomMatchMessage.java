package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import net.minecraft.util.Uuids;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.UUID;

public record DungeonRoomMatchMessage(String type, UUID uuid, Room.Type roomType, Room.Shape shape, Room.Direction direction,
									  String room, List<Vector2ic> pos) implements Message<DungeonRoomMatchMessage> {
	private static final Codec<Vector2ic> VECTOR2I_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("x").forGetter(Vector2ic::x),
			Codec.INT.fieldOf("y").forGetter(Vector2ic::y)
	).apply(instance, Vector2i::new));

	public static final String TYPE = "room_match";
	public static final Codec<DungeonRoomMatchMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonRoomMatchMessage::type),
					Uuids.STRING_CODEC.fieldOf("uuid").forGetter(DungeonRoomMatchMessage::uuid),
					Room.Type.CODEC.fieldOf("roomType").forGetter(DungeonRoomMatchMessage::roomType),
					Room.Shape.CODEC.fieldOf("shape").forGetter(DungeonRoomMatchMessage::shape),
					Room.Direction.CODEC.fieldOf("direction").forGetter(DungeonRoomMatchMessage::direction),
					Codec.STRING.fieldOf("room").forGetter(DungeonRoomMatchMessage::room),
					VECTOR2I_CODEC.listOf(1, 5).fieldOf("pos").forGetter(DungeonRoomMatchMessage::pos))
			.apply(instance, DungeonRoomMatchMessage::new));

	public DungeonRoomMatchMessage(UUID uuid, Room.Type roomType, Room.Shape shape, Room.Direction direction, String room, List<Vector2ic> pos) {
		this(TYPE, uuid, roomType, shape, direction, room, pos);
	}

	@Override
	public Codec<DungeonRoomMatchMessage> getCodec() {
		return CODEC;
	}
}
