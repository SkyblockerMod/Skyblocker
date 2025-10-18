package de.hysky.skyblocker.utils.ws;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringIdentifiable;

public enum Type implements StringIdentifiable {
	SUBSCRIBE("subscribe"),
	INITIAL_MESSAGE("initialMessage"),
	PUBLISH("publish"),
	RESPONSE("response"),
	UNSUBSCRIBE("unsubscribe");

	public static final Codec<Type> CODEC = StringIdentifiable.createBasicCodec(Type::values);

	private final String id;

	Type(String id) {
		this.id = id;
	}

	@Override
	public String asString() {
		return this.id;
	}
}
