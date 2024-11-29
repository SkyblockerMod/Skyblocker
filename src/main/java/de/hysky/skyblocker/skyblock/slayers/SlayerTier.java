package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public enum SlayerTier implements StringIdentifiable {
	UNKNOWN("unknown", Formatting.WHITE),
	I("i", Formatting.GREEN),
	II("ii", Formatting.YELLOW),
	III("iii", Formatting.RED),
	IV("iv", Formatting.DARK_RED),
	V("v", Formatting.DARK_PURPLE);
	public static final Codec<SlayerTier> CODEC = StringIdentifiable.createCodec(SlayerTier::values);
	public final String name;
	public final Formatting color;

	SlayerTier(String name, Formatting color) {
		this.name = name;
		this.color = color;
	}

	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	@Override
	public String asString() {
		return name;
	}
}
