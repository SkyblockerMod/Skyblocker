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
	public final String name;
	public final ChatFormatting formatting;
	public final int color;
	public final float r;
	public final float g;
	public final float b;

	SkyblockItemRarity(ChatFormatting formatting) {
		this.name = name().replace("_", " ");
		this.formatting = formatting;
		//noinspection DataFlowIssue
		this.color = formatting.getColor();

		this.r = ((color >> 16) & 0xFF) / 255f;
		this.g = ((color >> 8) & 0xFF) / 255f;
		this.b = (color & 0xFF) / 255f;
	}

	/**
	 * @return The amount of magic power an accessory with this rarity would give.
	 */
	public int getMP() {
		return switch (this) {
			case COMMON, SPECIAL -> 3;
			case UNCOMMON, VERY_SPECIAL -> 5;
			case RARE -> 8;
			case EPIC -> 12;
			case LEGENDARY -> 16;
			case MYTHIC -> 22;
			default -> 1;
		};
	}

	public SkyblockItemRarity recombobulate() {
		return switch (this) {
			case COMMON -> UNCOMMON;
			case UNCOMMON -> RARE;
			case RARE -> EPIC;
			case EPIC -> LEGENDARY;
			case LEGENDARY -> MYTHIC;
			case MYTHIC -> DIVINE;
			case DIVINE -> SPECIAL;
			case SPECIAL, VERY_SPECIAL, ULTIMATE -> VERY_SPECIAL;
			default -> UNKNOWN;
		};
	}

	@Override
	public String getSerializedName() {
		return name();
	}

	@Override
	public String toString() {
		return name;
	}

	public SkyblockItemRarity next() {
		return EnumUtils.cycle(this);
	}

	public static Optional<SkyblockItemRarity> containsName(String name) {
		// Find last because "UNCOMMON" contains "COMMON" and "VERY_SPECIAL" contains "SPECIAL"
		return Streams.findLast(Arrays.stream(SkyblockItemRarity.values())
				.filter(rarity -> name.contains(rarity.toString()))
		);
	}

	public static SkyblockItemRarity fromColor(int color) {
		return Arrays.stream(SkyblockItemRarity.values())
				.filter(rarity -> ARGB.colorFromFloat(1f, rarity.r, rarity.g, rarity.b) == ARGB.opaque(color))
				.findFirst()
				.orElse(UNKNOWN);
	}
}
