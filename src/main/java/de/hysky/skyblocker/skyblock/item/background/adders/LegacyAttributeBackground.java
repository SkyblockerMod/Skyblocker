package de.hysky.skyblocker.skyblock.item.background.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;

public class LegacyAttributeBackground extends ColoredItemBackground<Integer> {
	private static final int COLOR = 0xFFFF0000; // red

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.legacyAttributeBackgrounds;
	}

	@Override
	protected Integer getColorKey(ItemStack stack, Int2ReferenceOpenHashMap<Integer> cache) {
		if (stack == null || stack.isEmpty() || ItemUtils.getItemId(stack).equals("ATTRIBUTE_SHARD")) return null;

		int hashCode = System.identityHashCode(stack);
		if (cache.containsKey(hashCode)) return cache.get(hashCode);

		boolean hasAttributes = ItemUtils.getCustomData(stack).contains("attributes");
		cache.put(hashCode, hasAttributes ? COLOR : null);
		return hasAttributes ? COLOR : null;
	}

	@Override
	protected void draw(DrawContext context, int x, int y, Integer color) {
		context.drawSpriteStretched(RenderLayer::getGuiTextured, getSprite(), x, y, 16, 16, color);
	}
}
