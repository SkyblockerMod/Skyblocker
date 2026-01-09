package de.hysky.skyblocker.skyblock.item.background.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;

public class LegacyAttributeBackground extends ColoredItemBackground<Integer> {
	private static final int COLOR = 0xFFFF0000; // red

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.legacyAttributeBackgrounds;
	}

	@Override
	protected Integer getColorKey(ItemStack stack, Int2ReferenceOpenHashMap<Integer> cache) {
		if (stack == null || stack.isEmpty() || stack.getSkyblockId().equals("ATTRIBUTE_SHARD")) return null;

		int hashCode = System.identityHashCode(stack);
		if (cache.containsKey(hashCode)) return cache.get(hashCode);

		boolean hasAttributes = ItemUtils.getCustomData(stack).contains("attributes");
		cache.put(hashCode, hasAttributes ? COLOR : null);
		return hasAttributes ? COLOR : null;
	}

	@Override
	protected void draw(GuiGraphics context, int x, int y, Integer color) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, getSprite(), x, y, 16, 16, color);
	}
}
