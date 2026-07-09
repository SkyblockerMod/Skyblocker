package de.hysky.skyblocker.skyblock.item;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.SkyBlockColors;
import io.github.moulberry.repo.data.Rarity;

public enum SkyblockItemRarity implements StringRepresentable {
	COMMON(ChatFormatting.WHITE),
	UNCOMMON(ChatFormatting.GREEN),
	RARE(ChatFormatting.BLUE, SkyBlockColors.BLUE),
	EPIC(ChatFormatting.DARK_PURPLE, SkyBlockColors.DARK_PURPLE),
	LEGENDARY(ChatFormatting.GOLD, SkyBlockColors.GOLD),
	MYTHIC(ChatFormatting.LIGHT_PURPLE),
	DIVINE(ChatFormatting.AQUA),
	SPECIAL(ChatFormatting.RED),
	VERY_SPECIAL(ChatFormatting.RED),
	ULTIMATE(ChatFormatting.DARK_RED, SkyBlockColors.DARK_RED),
	ADMIN(ChatFormatting.DARK_RED, SkyBlockColors.DARK_RED),
	UNKNOWN(ChatFormatting.DARK_GRAY);

	public static final Codec<SkyblockItemRarity> CODEC = StringRepresentable.fromEnum(SkyblockItemRarity::values);
	public final String name;
	public final ChatFormatting formatting;
	public final int color;
	public final float r;
	public final float g;
	public final float b;

	SkyblockItemRarity(ChatFormatting formatting) {
		this(formatting, TextColor.fromLegacyFormat(formatting));
	}

	SkyblockItemRarity(ChatFormatting formatting, TextColor textColor) {
		this.name = name().replace("_", " ");
		this.formatting = formatting;
		//noinspection DataFlowIssue
		this.color = textColor.getValue();

		this.r = ((this.color >> 16) & 0xFF) / 255f;
		this.g = ((this.color >> 8) & 0xFF) / 255f;
		this.b = (this.color & 0xFF) / 255f;
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
