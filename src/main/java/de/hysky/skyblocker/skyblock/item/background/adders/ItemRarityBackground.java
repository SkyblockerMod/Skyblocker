package de.hysky.skyblocker.skyblock.item.background.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

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
	protected void draw(GuiGraphics context, int x, int y, SkyblockItemRarity rarity) {
		if (rarity == SkyblockItemRarity.UNKNOWN) return;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, getSprite(), x, y, 16, 16,
				ARGB.colorFromFloat(
						SkyblockerConfigManager.get().general.itemInfoDisplay.itemBackgroundOpacity,
						rarity.r, rarity.g, rarity.b
				)
		);
	}
}
