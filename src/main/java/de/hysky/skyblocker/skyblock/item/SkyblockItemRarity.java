package de.hysky.skyblocker.skyblock.item;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.utils.EnumUtils;

public enum SkyblockItemRarity implements StringRepresentable {
	COMMON(ChatFormatting.WHITE),
	UNCOMMON(ChatFormatting.GREEN),
	RARE(ChatFormatting.BLUE),
	EPIC(ChatFormatting.DARK_PURPLE),
	LEGENDARY(ChatFormatting.GOLD),
	MYTHIC(ChatFormatting.LIGHT_PURPLE),
	DIVINE(ChatFormatting.AQUA),
	SPECIAL(ChatFormatting.RED),
	VERY_SPECIAL(ChatFormatting.RED),
	ULTIMATE(ChatFormatting.DARK_RED),
	ADMIN(ChatFormatting.DARK_RED),
	UNKNOWN(ChatFormatting.DARK_GRAY);

	public static final Codec<SkyblockItemRarity> CODEC = StringRepresentable.fromEnum(SkyblockItemRarity::values);
	public final ChatFormatting formatting;
	public final int color;
	public final float r;
	public final float g;
	public final float b;

	SkyblockItemRarity(ChatFormatting formatting) {
		this.formatting = formatting;
		//noinspection DataFlowIssue
		this.color = formatting.getColor();

		this.r = ((color >> 16) & 0xFF) / 255f;
		this.g = ((color >> 8) & 0xFF) / 255f;
		this.b = (color & 0xFF) / 255f;
	}

	@Override
	public String getSerializedName() {
		return name();
	}

	public SkyblockItemRarity next() {
		return EnumUtils.cycle(this);
	}

	public static Optional<SkyblockItemRarity> containsName(String name) {
		// Find last because "UNCOMMON" contains "COMMON" and "VERY_SPECIAL" contains "SPECIAL"
		return Streams.findLast(Arrays.stream(SkyblockItemRarity.values())
				.filter(rarity -> name.contains(rarity.name()))
		);
	}

	public static SkyblockItemRarity fromColor(int color) {
		return Arrays.stream(SkyblockItemRarity.values())
				.filter(rarity -> ARGB.colorFromFloat(1f, rarity.r, rarity.g, rarity.b) == ARGB.opaque(color))
				.findFirst()
				.orElse(UNKNOWN);
	}
}
