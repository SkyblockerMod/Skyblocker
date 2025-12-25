package de.hysky.skyblocker.skyblock.item.tooltip;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CompactorPreviewTooltipComponent implements ClientTooltipComponent {
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final ResourceLocation DISABLED_SLOT = ResourceLocation.withDefaultNamespace("container/crafter/disabled_slot");
	private final Iterable<IntObjectPair<ItemStack>> items;
	private final IntIntPair dimensions;
	private final int columns;

	CompactorPreviewTooltipComponent(Iterable<IntObjectPair<ItemStack>> items, IntIntPair dimensions) {
		this.items = items;
		this.dimensions = dimensions;
		this.columns = Math.max(dimensions.rightInt(), 3);
	}

	@Override
	public int getHeight(Font textRenderer) {
		return dimensions.leftInt() * 18 + 17;
	}

	@Override
	public int getWidth(Font textRenderer) {
		return columns * 18 + 14;
	}

	/**
	 * Draws the items in the compactor/deletor.
	 *
	 * <p>Draws items on a background of {@code dimensions.leftInt()} rows and {@code columns} columns.
	 * Note that the minimum columns is 3 so the text "Contents" fits.
	 * If the compactor/deletor only has one column, draw a black stained glass pane to fill the first and third columns.
	 * 2 columns is not currently supported and will have an empty third column.
	 */
	@Override
	public void renderImage(Font textRenderer, int x, int y, int width, int height, GuiGraphics context) {
		// Draw the background with `dimensions.leftInt()` rows and `columns` columns with some texture math
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, columns * 18 + 7, dimensions.leftInt() * 18 + 17, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + columns * 18 + 7, y, 169, 0, 7, dimensions.leftInt() * 18 + 17, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + dimensions.leftInt() * 18 + 17, 0, 215, columns * 18 + 7, 7, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + columns * 18 + 7, y + dimensions.leftInt() * 18 + 17, 169, 215, 7, 7, 256, 256);

		//Draw name - I don't think it needs to be translatable
		context.drawString(textRenderer, "Contents", x + 8, y + 6, 0xFF404040, false);

		for (IntObjectPair<ItemStack> entry : items) {
			int itemX = x + entry.leftInt() % dimensions.rightInt() * 18 + 8;
			int itemY = y + entry.leftInt() / dimensions.rightInt() * 18 + 18;

			// Draw a disabled slot to fill the left slot if there is only one column
			if (dimensions.rightInt() == 1) {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, DISABLED_SLOT, itemX - 1, itemY - 1, 18, 18);
				itemX += 18;
			}
			if (entry.right() != null) {
				context.renderItem(entry.right(), itemX, itemY);
				context.renderItemDecorations(textRenderer, entry.right(), itemX, itemY);
			}
			// Draw a disabled slot to fill the right slot if there is only one column
			if (dimensions.rightInt() == 1) {
				itemX += 18;
				context.blitSprite(RenderPipelines.GUI_TEXTURED, DISABLED_SLOT, itemX - 1, itemY - 1, 18, 18);
			}
		}
	}
}
