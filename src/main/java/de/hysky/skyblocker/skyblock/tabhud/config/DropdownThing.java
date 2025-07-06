package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.function.Consumer;

public class DropdownThing<T> extends DropdownWidget<T> {
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_outer_space");

    public DropdownThing(int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
        super(client, x, y, width, maxHeight, 12, entries, selectCallback, selected);
        headerHeight = 15;
    }


    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        int y = getY() - 1;
        int y2 = y + headerHeight;
        context.drawVerticalLine(getX() - 1, y, y2, ColorHelper.withAlpha(15, -1));
        context.drawVerticalLine(getX(), y, y2, ColorHelper.withAlpha(100, 0));
        context.drawVerticalLine(getX() + 1, y, y2, ColorHelper.withAlpha(15, -1));


        context.drawVerticalLine(getRight() - 1, y, y2, ColorHelper.withAlpha(15, 0));
        context.drawVerticalLine(getRight(), y, y2, ColorHelper.withAlpha(100, 0));
        context.drawVerticalLine(getRight() + 1, y, y2, ColorHelper.withAlpha(15, 0));

        if (isHovered() && mouseY < y2) {
            context.fill(getX(), y, getRight(), y2, ColorHelper.withAlpha(100, 0));
        } else {
            context.fill(getX(), y, getRight(), y2, ColorHelper.withAlpha(50, 0));
        }
        drawScrollableText(context, client.textRenderer, Text.literal(
                        selected.toString()),
                getX() + 2,
                getY() + 2,
                getRight() - 2,
                getY() + headerHeight - 2,
                -1);
    }

    @Override
    protected void drawMenuListBackground(DrawContext context, int listX, int listY, int listWidth, int listHeight) {
        context.enableScissor(listX, listY - 1, listX + listWidth, listY + listHeight);
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, listX, listY - 3, listWidth, listHeight + 5);
        context.disableScissor();
    }

    @Override
    protected void renderEntry(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, T entry) {
        if (index != 0) {
            context.drawHorizontalLine(x, x + entryWidth, y, ColorHelper.withAlpha(15, 0));
        }
        drawScrollableText(context, client.textRenderer, formatter.apply(entry).copy().fillStyle(Style.EMPTY.withUnderline(hovered)), x, y, x + entryWidth, y + 11, -1);

    }
}
