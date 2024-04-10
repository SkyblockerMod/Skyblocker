package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

public class StatusBar implements Widget, Drawable, Element, Selectable {

    private static final Identifier BAR_FILL = new Identifier(SkyblockerMod.NAMESPACE, "bars/bar_fill");
    private static final Identifier BAR_BACK = new Identifier(SkyblockerMod.NAMESPACE, "bars/bar_back");

    private final Identifier icon;
    private final Color[] colors;
    private final boolean hasOverflow;
    private final @Nullable Color textColor;
    private final String name;

    private @Nullable Consumer<StatusBar> onClick = null;
    public int gridX = 0;
    public int gridY = 0;

    public int size = 1;
    private int width = 0;

    public float fill = 0;
    public float overflowFill = 0;

    private int x = 0;
    private int y = 0;

    public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow, @Nullable Color textColor, String name) {
        this.icon = icon;
        this.colors = colors;
        this.hasOverflow = hasOverflow;
        this.textColor = textColor;
        this.name = name;
    }

    public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow, @Nullable Color textColor){
        this(icon, colors, hasOverflow, textColor, "no name");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (width <= 0) return;
        context.drawGuiTexture(icon, x, y, 9, 9);
        context.drawGuiTexture(BAR_BACK, x + 10, y + 1, width - 10, 7);
        RenderHelper.renderNineSliceColored(context, BAR_FILL, x + 11, y + 2, (int) ((width - 12) * fill), 5, colors[0]);
        if (hasOverflow) {
            RenderHelper.renderNineSliceColored(context, BAR_FILL, x + 11, y + 2, (int) ((width - 12) * overflowFill), 5, colors[1]);
        }
        context.drawText(MinecraftClient.getInstance().textRenderer, gridX + " ; " + gridY , x, y-9, Colors.WHITE, true);
    }

    public void renderCursor(DrawContext context, int mouseX, int mouseY, float delta) {
        int temp_x = x;
        int temp_y = y;
        int temp_width = width;
        x = mouseX;
        y = mouseY;
        width = 100;
        render(context, mouseX, mouseY, delta);
        x = temp_x;
        y = temp_y;
        width = temp_width;
    }

    // GUI shenanigans

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Widget.super.getNavigationFocus();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + getWidth() && mouseY >= y && mouseY <= y + getHeight();
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {}

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (onClick != null) {
            onClick.accept(this);
        }
        return true;
    }

    public void setOnClick(@Nullable Consumer<StatusBar> onClick) {
        this.onClick = onClick;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("gridX", gridX)
                .append("gridY", gridY)
                .append("size", size)
                .append("x", x)
                .append("y", y)
                .append("width", width)
                .toString();
    }
}
