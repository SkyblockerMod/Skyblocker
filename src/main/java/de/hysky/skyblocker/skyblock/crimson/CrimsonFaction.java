package de.hysky.skyblocker.skyblock.crimson;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringIdentifiable;

public enum CrimsonFaction implements StringIdentifiable {
	MAGE,
	BARBARIAN;

	public static final Codec<CrimsonFaction> CODEC = StringIdentifiable.createBasicCodec(CrimsonFaction::values);

	@Override
	public String asString() {
		return this.name();
	}
}
