package de.hysky.skyblocker.utils.ws;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum Service implements StringRepresentable {
	CRYSTAL_WAYPOINTS,
	DUNGEON_SECRETS,
	EGG_WAYPOINTS;

	public static final Codec<Service> CODEC = StringRepresentable.fromValues(Service::values);

	@Override
	public String getSerializedName() {
		return name();
	}
}
