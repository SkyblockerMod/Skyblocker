package de.hysky.skyblocker.skyblock.item.tooltip;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class CompactorPreviewTooltipComponent implements TooltipComponent {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
    private static final ItemStack BLACK_STAINED_GLASS_PANE = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
    private final Iterable<IntObjectPair<ItemStack>> items;
    private final IntIntPair dimensions;
    private final int columns;

    CompactorPreviewTooltipComponent(Iterable<IntObjectPair<ItemStack>> items, IntIntPair dimensions) {
        this.items = items;
        this.dimensions = dimensions;
        this.columns = Math.max(dimensions.rightInt(), 3);
    }

    @Override
    public int getHeight() {
        return dimensions.leftInt() * 18 + 17;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
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
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        // Draw the background with `dimensions.leftInt()` rows and `columns` columns with some texture math
        context.drawTexture(TEXTURE, x, y, 0, 0, columns * 18 + 7, dimensions.leftInt() * 18 + 17);
        context.drawTexture(TEXTURE, x + columns * 18 + 7, y, 169, 0, 7, dimensions.leftInt() * 18 + 17);
        context.drawTexture(TEXTURE, x, y + dimensions.leftInt() * 18 + 17, 0, 215, columns * 18 + 7, 7);
        context.drawTexture(TEXTURE, x + columns * 18 + 7, y + dimensions.leftInt() * 18 + 17, 169, 215, 7, 7);

        //Draw name - I don't think it needs to be translatable
        context.drawText(textRenderer, "Contents", x + 8, y + 6, 0x404040, false);

        for (IntObjectPair<ItemStack> entry : items) {
            int itemX = x + entry.leftInt() % dimensions.rightInt() * 18 + 8;
            int itemY = y + entry.leftInt() / dimensions.rightInt() * 18 + 18;

            // Draw a black stained glass pane to fill the left slot if there is only one column
            if (dimensions.rightInt() == 1) {
                context.drawItem(BLACK_STAINED_GLASS_PANE, itemX, itemY);
                context.drawItemInSlot(textRenderer, BLACK_STAINED_GLASS_PANE, itemX, itemY);
                itemX += 18;
            }
            if (entry.right() != null) {
                context.drawItem(entry.right(), itemX, itemY);
                context.drawItemInSlot(textRenderer, entry.right(), itemX, itemY);
            }
            // Draw a black stained glass pane to fill the right slot if there is only one column
            if (dimensions.rightInt() == 1) {
                itemX += 18;
                context.drawItem(BLACK_STAINED_GLASS_PANE, itemX, itemY);
                context.drawItemInSlot(textRenderer, BLACK_STAINED_GLASS_PANE, itemX, itemY);
            }
        }
    }
}
