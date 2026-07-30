package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record DungeonBatKilledMessage(String type, UUID sender) implements Message<DungeonBatKilledMessage> {
	public static final String TYPE = "bat_killed";
	public static final Codec<DungeonBatKilledMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonBatKilledMessage::type),
					UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(DungeonBatKilledMessage::sender))
			.apply(instance, DungeonBatKilledMessage::new));

	public DungeonBatKilledMessage(UUID uuid) {
		this(TYPE, uuid);
	}

	@Override
	public Codec<DungeonBatKilledMessage> getCodec() {
		return CODEC;
	}
}
