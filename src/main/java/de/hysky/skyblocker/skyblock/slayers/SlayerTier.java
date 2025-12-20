package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public enum SlayerTier implements StringIdentifiable {
	I("I", Formatting.GREEN),
	II("II", Formatting.YELLOW),
	III("III", Formatting.RED),
	IV("IV", Formatting.DARK_RED),
	V("V", Formatting.DARK_PURPLE);

	public static final Codec<SlayerTier> CODEC = StringIdentifiable.createCodec(SlayerTier::values);
	public final String name;
	public final Formatting color;

	SlayerTier(String name, Formatting color) {
		this.name = name;
		this.color = color;
	}

	public static SlayerTier valueOf(@Nullable String name, String slayerName) {
		// These don't have the tier in their names (armorStand), so name parameter is null
		if (slayerName.contains("Conjoined Brood")) return SlayerTier.V;
		if (slayerName.contains("Atoned Horror")) return SlayerTier.V;
		if (slayerName.contains("Bloodfiend") && name == null) return SlayerTier.V;
		if (name == null) return SlayerTier.I;
		return SlayerTier.valueOf(name);
	}

	@Override
	public String asString() {
		return name;
	}
}
