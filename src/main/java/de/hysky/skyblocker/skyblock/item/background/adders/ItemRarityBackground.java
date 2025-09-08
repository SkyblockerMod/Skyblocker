package de.hysky.skyblocker.skyblock.item.background.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;

public class ItemRarityBackground extends ColoredItemBackground<SkyblockItemRarity> {
	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds;
	}

	@Override
	protected SkyblockItemRarity getColorKey(ItemStack stack, Int2ReferenceOpenHashMap<SkyblockItemRarity> cache) {
		return stack.getSkyblockRarity();
	}

	@Override
	protected void draw(DrawContext context, int x, int y, SkyblockItemRarity rarity) {
		if (rarity == SkyblockItemRarity.UNKNOWN) return;
		context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, getSprite(), x, y, 16, 16,
				ColorHelper.fromFloats(
						SkyblockerConfigManager.get().general.itemInfoDisplay.itemBackgroundOpacity,
						rarity.r, rarity.g, rarity.b
				)
		);
	}
}
