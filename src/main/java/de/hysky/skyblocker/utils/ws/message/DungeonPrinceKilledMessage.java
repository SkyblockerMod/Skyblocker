package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record DungeonPrinceKilledMessage(String type, UUID sender) implements Message<DungeonPrinceKilledMessage> {
	public static final String TYPE = "prince_killed";
	public static final Codec<DungeonPrinceKilledMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonPrinceKilledMessage::type),
					Uuids.STRING_CODEC.fieldOf("uuid").forGetter(DungeonPrinceKilledMessage::sender))
			.apply(instance, DungeonPrinceKilledMessage::new));

	public DungeonPrinceKilledMessage(UUID uuid) {
		this(TYPE, uuid);
	}

	@Override
	public Codec<DungeonPrinceKilledMessage> getCodec() {
		return CODEC;
	}
}
