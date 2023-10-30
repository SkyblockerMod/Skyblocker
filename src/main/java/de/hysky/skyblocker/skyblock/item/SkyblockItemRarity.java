package de.hysky.skyblocker.skyblock.item;

import net.minecraft.util.Formatting;

public enum SkyblockItemRarity {
	ADMIN(Formatting.DARK_RED),
	VERY_SPECIAL(Formatting.RED),
	SPECIAL(Formatting.RED),
	DIVINE(Formatting.AQUA),
	MYTHIC(Formatting.LIGHT_PURPLE),
	LEGENDARY(Formatting.GOLD),
	EPIC(Formatting.DARK_PURPLE),
	RARE(Formatting.BLUE),
	UNCOMMON(Formatting.GREEN),
	COMMON(Formatting.WHITE);

	public final float r;
	public final float g;
	public final float b;

	SkyblockItemRarity(Formatting formatting) {
		@SuppressWarnings("DataFlowIssue")
		int rgb = formatting.getColorValue();

		this.r = ((rgb >> 16) & 0xFF) / 255f;
		this.g = ((rgb >> 8) & 0xFF) / 255f;
		this.b = (rgb & 0xFF) / 255f;
	}
}
