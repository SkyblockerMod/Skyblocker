package de.hysky.skyblocker.skyblock.item;

import net.minecraft.util.Formatting;

public enum SkyblockItemRarity {
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
	ADMIN(Formatting.DARK_RED);

	public final int color;
	public final float r;
	public final float g;
	public final float b;

	SkyblockItemRarity(Formatting formatting) {
        //noinspection DataFlowIssue
        this.color = formatting.getColorValue();

		this.r = ((color >> 16) & 0xFF) / 255f;
		this.g = ((color >> 8) & 0xFF) / 255f;
		this.b = (color & 0xFF) / 255f;
	}
}
