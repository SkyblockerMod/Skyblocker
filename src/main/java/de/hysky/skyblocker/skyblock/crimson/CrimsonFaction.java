package de.hysky.skyblocker.skyblock.crimson;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum CrimsonFaction implements StringRepresentable {
	MAGE,
	BARBARIAN;

	public static final Codec<CrimsonFaction> CODEC = StringRepresentable.fromValues(CrimsonFaction::values);

	@Override
	public String getSerializedName() {
		return this.name();
	}
}
