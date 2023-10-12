package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CompactorPreviewTooltipComponent implements TooltipComponent {
    private static final Identifier INVENTORY_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/inventory_background.png");
    private final Iterable<IntObjectPair<ItemStack>> items;
    private final IntIntPair dimensions;

    public CompactorPreviewTooltipComponent(Iterable<IntObjectPair<ItemStack>> items, IntIntPair dimensions) {
        this.items = items;
        this.dimensions = dimensions;
    }

    @Override
    public int getHeight() {
        return dimensions.leftInt() * 18 + 14;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return dimensions.rightInt() * 18 + 14;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        context.drawTexture(INVENTORY_TEXTURE, x, y, 0, 0, 7 + dimensions.rightInt() * 18, 7);
        context.drawTexture(INVENTORY_TEXTURE, x + 7 + dimensions.rightInt() * 18, y, 169, 0, 7, 7);

        for (int i = 0; i < dimensions.leftInt(); i++) {
            context.drawTexture(INVENTORY_TEXTURE, x, y + 7 + i * 18, 0, 7, 7, 18);
            for (int j = 0; j < dimensions.rightInt(); j++) {
                context.drawTexture(INVENTORY_TEXTURE, x + 7 + j * 18, y + 7 + i * 18, 7, 7, 18, 18);
            }
            context.drawTexture(INVENTORY_TEXTURE, x + 7 + dimensions.rightInt() * 18, y + 7 + i * 18, 169, 7, 7, 18);
        }
        context.drawTexture(INVENTORY_TEXTURE, x, y + 7 + dimensions.leftInt() * 18, 0, 25, 7 + dimensions.rightInt() * 18, 7);
        context.drawTexture(INVENTORY_TEXTURE, x + 7 + dimensions.rightInt() * 18, y + 7 + dimensions.leftInt() * 18, 169, 25, 7, 7);

        for (IntObjectPair<ItemStack> entry : items) {
            if (entry.right() != null) {
                int itemX = x + entry.leftInt() % dimensions.rightInt() * 18 + 8;
                int itemY = y + entry.leftInt() / dimensions.rightInt() * 18 + 8;
                context.drawItem(entry.right(), itemX, itemY);
                context.drawItemInSlot(textRenderer, entry.right(), itemX, itemY);
            }
        }
    }
}
