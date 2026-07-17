package de.hysky.skyblocker.skyblock.item;

import java.util.Arrays;
import java.util.Optional;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;

import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.SkyBlockColors;
import io.github.moulberry.repo.data.Rarity;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;

public enum SkyblockItemRarity implements StringRepresentable {
	COMMON(TextColor.WHITE),
	UNCOMMON(TextColor.GREEN),
	RARE(SkyBlockColors.BLUE, TextColor.BLUE),
	EPIC(SkyBlockColors.DARK_PURPLE, TextColor.DARK_PURPLE),
	LEGENDARY(SkyBlockColors.GOLD, TextColor.GOLD),
	MYTHIC(TextColor.LIGHT_PURPLE),
	DIVINE(TextColor.AQUA),
	SPECIAL(TextColor.RED),
	VERY_SPECIAL(TextColor.RED),
	ULTIMATE(SkyBlockColors.DARK_RED, TextColor.DARK_RED),
	ADMIN(SkyBlockColors.DARK_RED, TextColor.DARK_RED),
	UNKNOWN(TextColor.DARK_GRAY);

	public static final Codec<SkyblockItemRarity> CODEC = StringRepresentable.fromEnum(SkyblockItemRarity::values);
	public final String name;
	public final int color;
	public final float r;
	public final float g;
	public final float b;
	public final RarityColor legacyColor;

	SkyblockItemRarity(TextColor color, TextColor legacyColor) {
		this.name = this.name().replace("_", " ");
		this.color = color.getValue();

		this.r = ARGB.redFloat(this.color);
		this.g = ARGB.greenFloat(this.color);
		this.b = ARGB.blueFloat(this.color);
		this.legacyColor = new RarityColor(legacyColor);
	}

	SkyblockItemRarity(TextColor color) {
		this(color, color);
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

	public Rarity toNeuRarity() {
		return switch (this) {
			case SkyblockItemRarity.COMMON -> Rarity.COMMON;
			case SkyblockItemRarity.UNCOMMON -> Rarity.UNCOMMON;
			case SkyblockItemRarity.RARE -> Rarity.RARE;
			case SkyblockItemRarity.EPIC -> Rarity.EPIC;
			case SkyblockItemRarity.LEGENDARY -> Rarity.LEGENDARY;
			case SkyblockItemRarity.MYTHIC -> Rarity.MYTHIC;
			case SkyblockItemRarity.DIVINE -> Rarity.DIVINE;
			case SkyblockItemRarity.SPECIAL -> Rarity.SPECIAL;
			case SkyblockItemRarity.VERY_SPECIAL -> Rarity.VERY_SPECIAL;
			case SkyblockItemRarity.ULTIMATE -> Rarity.SUPREME;
			case SkyblockItemRarity.ADMIN, SkyblockItemRarity.UNKNOWN -> Rarity.UNKNOWN;
		};
	}

	@Override
	public String getSerializedName() {
		return this.name();
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
				.filter(rarity -> ARGB.opaque(rarity.color) == ARGB.opaque(color))
				.findFirst()
				.orElse(UNKNOWN);
	}

	public record RarityColor(int rgb, float r, float g, float b) {
		public RarityColor(TextColor textColor) {
			this(textColor.getValue(), ARGB.redFloat(textColor.getValue()), ARGB.greenFloat(textColor.getValue()), ARGB.blueFloat(textColor.getValue()));
		}
	}
}
