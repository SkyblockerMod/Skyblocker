package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;

import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Abstract base class for a Widget.
 * Widgets are containers for components with a border and a title.
 * Their size is dependent on the components inside,
 * the position may be changed after construction.
 */
public abstract class Widget {

    private final ArrayList<Component> components = new ArrayList<>();
    private int w = 0, h = 0;
    private int x = 0, y = 0;
    private final int color;
    private final Text title;

    private static final TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;

    static final int BORDER_SZE_N = txtRend.fontHeight + 4;
    static final int BORDER_SZE_S = 4;
    static final int BORDER_SZE_W = 4;
    static final int BORDER_SZE_E = 4;
    static final int COL_BG_BOX = 0xc00c0c0c;

    public Widget(MutableText title, Integer colorValue) {
        this.title = title;
        this.color = 0xff000000 | colorValue;
    }

    public final void addComponent(Component c) {
        this.components.add(c);
    }

    public final void update() {
        this.components.clear();
        this.updateContent();
        this.pack();
    }

    public abstract void updateContent();

    /**
     * Shorthand function for simple components.
     * If the entry at idx has the format "<textA>: <textB>", an IcoTextComponent is
     * added as such:
     * [ico] [string] [textB.formatted(fmt)]
     */
    public final void addSimpleIcoText(ItemStack ico, String string, Formatting fmt, int idx) {
        Text txt = Widget.simpleEntryText(idx, string, fmt);
        this.addComponent(new IcoTextComponent(ico, txt));
    }

    public final void addSimpleIcoText(ItemStack ico, String string, Formatting fmt, String content) {
        Text txt = Widget.simpleEntryText(content, string, fmt);
        this.addComponent(new IcoTextComponent(ico, txt));
    }

    /**
     * Calculate the size of this widget.
     * <b>Must be called before returning from the widget constructor and after all
     * components are added!</b>
     */
    private void pack() {
        h = 0;
        w = 0;
        for (Component c : components) {
            h += c.getHeight() + Component.PAD_L;
            w = Math.max(w, c.getWidth() + Component.PAD_S);
        }

        h -= Component.PAD_L / 2; // less padding after lowest/last component
        h += BORDER_SZE_N + BORDER_SZE_S - 2;
        w += BORDER_SZE_E + BORDER_SZE_W;

        // min width is dependent on title
        w = Math.max(w, BORDER_SZE_W + BORDER_SZE_E + Widget.txtRend.getWidth(title) + 4 + 4 + 1);
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final int getY() {
        return this.y;
    }

    public final int getX() {
        return this.x;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final int getWidth() {
        return this.w;
    }

    public final int getHeight() {
        return this.h;
    }

    /**
     * Draw this widget with a background
     */
    public final void render(DrawContext context) {
        this.render(context, true);
    }

    /**
     * Draw this widget, possibly with a background
     */
    public final void render(DrawContext context, boolean hasBG) {
        MatrixStack ms = context.getMatrices();

        // not sure if this is the way to go, but it fixes Z-layer issues
        // like blocks being rendered behind the BG and the hotbar clipping into things
        RenderSystem.enableDepthTest();
        ms.push();

        float scale = SkyblockerConfigManager.get().general.tabHud.tabHudScale / 100f;
        ms.scale(scale, scale, 1);

        // move above other UI elements
        ms.translate(0, 0, 200);
        if (hasBG) {
            context.fill(x + 1, y, x + w - 1, y + h, COL_BG_BOX);
            context.fill(x, y + 1, x + 1, y + h - 1, COL_BG_BOX);
            context.fill(x + w - 1, y + 1, x + w, y + h - 1, COL_BG_BOX);
        }
        // move above background (if exists)
        ms.translate(0, 0, 100);

        int strHeightHalf = Widget.txtRend.fontHeight / 2;
        int strAreaWidth = Widget.txtRend.getWidth(title) + 4;

        context.drawText(txtRend, title, x + 8, y + 2, this.color, false);

        this.drawHLine(context, x + 2, y + 1 + strHeightHalf, 4);
        this.drawHLine(context, x + 2 + strAreaWidth + 4, y + 1 + strHeightHalf, w - 4 - 4 - strAreaWidth);
        this.drawHLine(context, x + 2, y + h - 2, w - 4);

        this.drawVLine(context, x + 1, y + 2 + strHeightHalf, h - 4 - strHeightHalf);
        this.drawVLine(context, x + w - 2, y + 2 + strHeightHalf, h - 4 - strHeightHalf);

        int yOffs = y + BORDER_SZE_N;

        for (Component c : components) {
            c.render(context, x + BORDER_SZE_W, yOffs);
            yOffs += c.getHeight() + Component.PAD_L;
        }
        // pop manipulations above
        ms.pop();
        RenderSystem.disableDepthTest();
    }

    private void drawHLine(DrawContext context, int xpos, int ypos, int width) {
        context.fill(xpos, ypos, xpos + width, ypos + 1, this.color);
    }

    private void drawVLine(DrawContext context, int xpos, int ypos, int height) {
        context.fill(xpos, ypos, xpos + 1, ypos + height, this.color);
    }

    /**
     * If the entry at idx has the format "[textA]: [textB]", the following is
     * returned:
     * [entryName] [textB.formatted(contentFmt)]
     */
    public static Text simpleEntryText(int idx, String entryName, Formatting contentFmt) {

        String src = PlayerListMgr.strAt(idx);

        if (src == null) {
            return null;
        }

        int cidx = src.indexOf(':');
        if (cidx == -1) {
            return null;
        }

        src = src.substring(src.indexOf(':') + 1);
        return Widget.simpleEntryText(src, entryName, contentFmt);
    }

    /**
     * @return [entryName] [entryContent.formatted(contentFmt)]
     */
    public static Text simpleEntryText(String entryContent, String entryName, Formatting contentFmt) {
        return Text.literal(entryName).append(Text.literal(entryContent).formatted(contentFmt));
    }

    /**
     * @return the entry at idx as unformatted Text
     */
    public static Text plainEntryText(int idx) {
        String str = PlayerListMgr.strAt(idx);
        if (str == null) {
            return null;
        }
        return Text.of(str);
    }
}
