package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public enum SlayerTier implements StringRepresentable {
	UNKNOWN("unknown", ChatFormatting.WHITE),
	I("I", ChatFormatting.GREEN),
	II("II", ChatFormatting.YELLOW),
	III("III", ChatFormatting.RED),
	IV("IV", ChatFormatting.DARK_RED),
	V("V", ChatFormatting.DARK_PURPLE);
	public static final Codec<SlayerTier> CODEC = StringRepresentable.fromEnum(SlayerTier::values);
	public final String name;
	public final ChatFormatting color;

	SlayerTier(String name, ChatFormatting color) {
		this.name = name;
		this.color = color;
	}

	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
