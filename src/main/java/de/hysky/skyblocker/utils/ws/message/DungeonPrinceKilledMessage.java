package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record DungeonPrinceKilledMessage(String type, UUID sender) implements Message<DungeonPrinceKilledMessage> {
	public static final String TYPE = "prince_killed";
	public static final Codec<DungeonPrinceKilledMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonPrinceKilledMessage::type),
					UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(DungeonPrinceKilledMessage::sender))
			.apply(instance, DungeonPrinceKilledMessage::new));

	public DungeonPrinceKilledMessage(UUID uuid) {
		this(TYPE, uuid);
	}

	@Override
	public Codec<DungeonPrinceKilledMessage> getCodec() {
		return CODEC;
	}
}
