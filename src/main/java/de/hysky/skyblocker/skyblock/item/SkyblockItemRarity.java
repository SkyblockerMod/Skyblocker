package de.hysky.skyblocker.skyblock.item;

import java.util.Arrays;
import java.util.Optional;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.utils.EnumUtils;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.ColorHelper;

public enum SkyblockItemRarity implements StringIdentifiable {
	COMMON(Formatting.WHITE),
	UNCOMMON(Formatting.GREEN),
	RARE(Formatting.BLUE),
	EPIC(Formatting.DARK_PURPLE),
	LEGENDARY(Formatting.GOLD),
	MYTHIC(Formatting.LIGHT_PURPLE),
	DIVINE(Formatting.AQUA),
	SPECIAL(Formatting.RED),
	VERY_SPECIAL(Formatting.RED),
	ULTIMATE(Formatting.DARK_RED),
	ADMIN(Formatting.DARK_RED),
	UNKNOWN(Formatting.DARK_GRAY);

	public static final Codec<SkyblockItemRarity> CODEC = StringIdentifiable.createCodec(SkyblockItemRarity::values);
	public final Formatting formatting;
	public final int color;
	public final float r;
	public final float g;
	public final float b;

	SkyblockItemRarity(Formatting formatting) {
		this.formatting = formatting;
		//noinspection DataFlowIssue
		this.color = formatting.getColorValue();

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
			case LEGENDARY -> 13;
			case MYTHIC -> 22;
			default -> 1;
		};
	}

	public SkyblockItemRarity recombulate() {
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
	public String asString() {
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
				.filter(rarity -> ColorHelper.fromFloats(1f, rarity.r, rarity.g, rarity.b) == ColorHelper.fullAlpha(color))
				.findFirst()
				.orElse(UNKNOWN);
	}
}
