package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record DungeonRoomSecretCountMessage(String type, UUID sender, String roomName, int secretCount) implements Message<DungeonRoomSecretCountMessage> {
	public static final String TYPE = "room_secret_count";
	public static final Codec<DungeonRoomSecretCountMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonRoomSecretCountMessage::type),
					UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(DungeonRoomSecretCountMessage::sender),
					Codec.STRING.fieldOf("room").forGetter(DungeonRoomSecretCountMessage::roomName),
					Codec.INT.fieldOf("secretCount").forGetter(DungeonRoomSecretCountMessage::secretCount))
			.apply(instance, DungeonRoomSecretCountMessage::new));

	public DungeonRoomSecretCountMessage(UUID uuid, String roomName, int secretCount) {
		this(TYPE, uuid, roomName, secretCount);
	}

	@Override
	public Codec<DungeonRoomSecretCountMessage> getCodec() {
		return CODEC;
	}
}
