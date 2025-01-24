package de.hysky.skyblocker.skyblock.item;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.utils.EnumUtils;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

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

	@Override
	public String asString() {
		return name();
	}

	public SkyblockItemRarity next() {
		return EnumUtils.cycle(this);
	}
}
