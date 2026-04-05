package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record DungeonMimicKilledMessage(String type, UUID sender) implements Message<DungeonMimicKilledMessage> {
	public static final String TYPE = "mimic_killed";
	public static final Codec<DungeonMimicKilledMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonMimicKilledMessage::type),
					UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(DungeonMimicKilledMessage::sender))
			.apply(instance, DungeonMimicKilledMessage::new));

	public DungeonMimicKilledMessage(UUID uuid) {
		this(TYPE, uuid);
	}

	@Override
	public Codec<DungeonMimicKilledMessage> getCodec() {
		return CODEC;
	}
}
