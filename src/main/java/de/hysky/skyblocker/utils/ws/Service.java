package de.hysky.skyblocker.utils.ws;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringIdentifiable;

public enum Service implements StringIdentifiable {
	CRYSTAL_WAYPOINTS,
	EGG_WAYPOINTS;

	public static final Codec<Service> CODEC = StringIdentifiable.createBasicCodec(Service::values);

	@Override
	public String asString() {
		return name();
	}
}
