package de.hysky.skyblocker.skyblock.item.tooltip;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CompactorPreviewTooltipComponent implements TooltipComponent {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
    private final Iterable<IntObjectPair<ItemStack>> items;
    private final IntIntPair dimensions;

    CompactorPreviewTooltipComponent(Iterable<IntObjectPair<ItemStack>> items, IntIntPair dimensions) {
        this.items = items;
        this.dimensions = dimensions;
    }

    @Override
    public int getHeight() {
        return dimensions.leftInt() * 18 + 17;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 176;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {        
        context.drawTexture(TEXTURE, x, y, 0, 0, 176, dimensions.leftInt() * 18 + 17);
        context.drawTexture(TEXTURE, x, y + dimensions.leftInt() * 18 + 17, 0, 215, 176, 7);

        //Draw name - I don't think it needs to be translatable
        context.drawText(textRenderer, "Contents", x + 8, y + 6, 0x404040, false);

        for (IntObjectPair<ItemStack> entry : items) {
            if (entry.right() != null) {
                int itemX = x + entry.leftInt() % dimensions.rightInt() * 18 + 8;
                int itemY = y + entry.leftInt() / dimensions.rightInt() * 18 + 18;
                context.drawItem(entry.right(), itemX, itemY);
                context.drawItemInSlot(textRenderer, entry.right(), itemX, itemY);
            }
        }
    }
}
