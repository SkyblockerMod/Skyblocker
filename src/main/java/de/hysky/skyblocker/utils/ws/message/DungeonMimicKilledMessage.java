package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record DungeonMimicKilledMessage(String type, UUID uuid) implements Message<DungeonMimicKilledMessage> {
	public static final String TYPE = "mimic_killed";
	public static final Codec<DungeonMimicKilledMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("type").forGetter(DungeonMimicKilledMessage::type),
					Uuids.STRING_CODEC.fieldOf("uuid").forGetter(DungeonMimicKilledMessage::uuid))
			.apply(instance, DungeonMimicKilledMessage::new));

	public DungeonMimicKilledMessage(UUID uuid) {
		this(TYPE, uuid);
	}

	@Override
	public Codec<DungeonMimicKilledMessage> getCodec() {
		return CODEC;
	}
}
