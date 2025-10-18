package de.hysky.skyblocker.utils.ws;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;

record Payload(Type type, Service service, String serverId, Optional<Dynamic<?>> message) {
	static final Codec<Payload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Type.CODEC.fieldOf("type").forGetter(Payload::type),
			Service.CODEC.fieldOf("service").forGetter(Payload::service),
			Codec.STRING.fieldOf("serverId").forGetter(Payload::serverId),
			Codec.PASSTHROUGH.optionalFieldOf("message").forGetter(Payload::message))
			.apply(instance, Payload::new));
}
