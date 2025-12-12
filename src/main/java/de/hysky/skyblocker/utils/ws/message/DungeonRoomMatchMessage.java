package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.CodecUtils;
import net.minecraft.util.Uuids;
import org.joml.Vector2ic;

import java.util.List;
import java.util.UUID;

public record DungeonRoomMatchMessage(String type, UUID sender, Room.Type roomType, Room.Shape shape, Room.Direction direction,
									String room, List<Vector2ic> pos) implements Message<DungeonRoomMatchMessage> {
	public static final String TYPE = "room_match";
	public static final Codec<DungeonRoomMatchMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonRoomMatchMessage::type),
					Uuids.STRING_CODEC.fieldOf("uuid").forGetter(DungeonRoomMatchMessage::sender),
					Room.Type.CODEC.fieldOf("roomType").forGetter(DungeonRoomMatchMessage::roomType),
					Room.Shape.CODEC.fieldOf("shape").forGetter(DungeonRoomMatchMessage::shape),
					Room.Direction.CODEC.fieldOf("direction").forGetter(DungeonRoomMatchMessage::direction),
					Codec.STRING.fieldOf("room").forGetter(DungeonRoomMatchMessage::room),
					CodecUtils.VECTOR_2I.listOf(1, 5).fieldOf("pos").forGetter(DungeonRoomMatchMessage::pos))
			.apply(instance, DungeonRoomMatchMessage::new));

	public DungeonRoomMatchMessage(UUID uuid, Room.Type roomType, Room.Shape shape, Room.Direction direction, String room, List<Vector2ic> pos) {
		this(TYPE, uuid, roomType, shape, direction, room, pos);
	}

	@Override
	public Codec<DungeonRoomMatchMessage> getCodec() {
		return CODEC;
	}
}
