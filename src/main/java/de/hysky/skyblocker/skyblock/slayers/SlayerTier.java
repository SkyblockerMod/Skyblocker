package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum SlayerTier implements StringRepresentable {
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

	public static SlayerTier valueOf(@Nullable String name, String slayerName) {
		// These don't have the tier in their names (armorStand), so name parameter is null
		if (slayerName.contains("Conjoined Brood")) return SlayerTier.V;
		if (slayerName.contains("Atoned Horror")) return SlayerTier.V;
		if (slayerName.contains("Bloodfiend") && name == null) return SlayerTier.V;
		if (name == null) return SlayerTier.I;
		return SlayerTier.valueOf(name);
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
