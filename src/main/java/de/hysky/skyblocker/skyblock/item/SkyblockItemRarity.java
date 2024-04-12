package de.hysky.skyblocker.skyblock.item;

import net.minecraft.util.Formatting;

public enum SkyblockItemRarity {
	ADMIN(Formatting.DARK_RED),
	ULTIMATE(Formatting.DARK_RED),
	VERY_SPECIAL(Formatting.RED),
	SPECIAL(Formatting.RED),
	DIVINE(Formatting.AQUA),
	MYTHIC(Formatting.LIGHT_PURPLE),
	LEGENDARY(Formatting.GOLD),
	EPIC(Formatting.DARK_PURPLE),
	RARE(Formatting.BLUE),
	UNCOMMON(Formatting.GREEN),
	COMMON(Formatting.WHITE);

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
