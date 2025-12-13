package de.hysky.skyblocker.utils.ws;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum Type implements StringRepresentable {
	SUBSCRIBE("subscribe"),
	INITIAL_MESSAGE("initialMessage"),
	PUBLISH("publish"),
	RESPONSE("response"),
	UNSUBSCRIBE("unsubscribe");

	public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);

	private final String id;

	Type(String id) {
		this.id = id;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
