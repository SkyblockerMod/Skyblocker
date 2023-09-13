package me.xmrvizzy.skyblocker.skyblock.item;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Map;

public class CompactorPreviewTooltipComponent implements TooltipComponent {

    private static final Identifier INVENTORY_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/inventory_background.png");

    Map<Integer, ItemStack> items;
    int[] dimensions;

    public CompactorPreviewTooltipComponent(Map<Integer, ItemStack> items, int[] dimensions) {
        this.items = items;
        this.dimensions = dimensions;
    }
    @Override
    public int getHeight() {
        return dimensions[0] * 18 + 14;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return dimensions[1] * 18 + 14;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        context.drawTexture(INVENTORY_TEXTURE, x, y, 0, 0, 7 + dimensions[1] * 18, 7);
        context.drawTexture(INVENTORY_TEXTURE, x + 7 + dimensions[1] * 18, y, 169, 0, 7, 7);

        for (int i = 0; i < dimensions[0]; i++) {
            context.drawTexture(INVENTORY_TEXTURE, x, y + 7 + i * 18, 0, 7, 7, 18);
            for (int j = 0; j < dimensions[1]; j++) {
                context.drawTexture(INVENTORY_TEXTURE, x + 7 + j * 18, y + 7 + i * 18, 7, 7, 18, 18);
            }
            context.drawTexture(INVENTORY_TEXTURE, x + 7 + dimensions[1] * 18, y + 7 + i * 18, 169, 7, 7, 18);
        }
        context.drawTexture(INVENTORY_TEXTURE, x, y + 7 + dimensions[0] * 18, 0, 25, 7 + dimensions[1] * 18, 7);
        context.drawTexture(INVENTORY_TEXTURE, x + 7 + dimensions[1] * 18, y + 7 + dimensions[0] * 18, 169, 25, 7, 7);

        MatrixStack matrices = context.getMatrices();
        for (Integer i : items.keySet()) {
            int itemX = x + i % dimensions[1] * 18 + 8;
            int itemY = y + i / dimensions[1] * 18 + 8;
            matrices.push();
            matrices.translate(0, 0, 200);
            context.drawItem(items.get(i), itemX, itemY);
            context.drawItemInSlot(textRenderer, items.get(i), itemX, itemY);
            matrices.pop();
        }
    }
}
