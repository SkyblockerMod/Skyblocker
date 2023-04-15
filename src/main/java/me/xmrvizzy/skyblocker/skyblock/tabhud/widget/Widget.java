package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.Component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public abstract class Widget {

    private ArrayList<Component> components = new ArrayList<>();
    private int w = 0, h = 0;
    private int x = 0, y = 0;
    private int color;
    private Text title;

    private static TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;

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
        components.add(c);
    }

    public final void pack() {
        for (Component c : components) {
            h += c.getHeight() + 4;
            w = Math.max(w, c.getWidth());
        }
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

    public final void render(MatrixStack ms) {
        this.render(ms, true);
    }

    public final void render(MatrixStack ms, boolean hasBG) {

        if (hasBG) {
            DrawableHelper.fill(ms, x + 1, y, x + w - 1, y + h, COL_BG_BOX);
            DrawableHelper.fill(ms, x, y + 1, x + 1, y + h - 1, COL_BG_BOX);
            DrawableHelper.fill(ms, x + w - 1, y + 1, x + w, y + h - 1, COL_BG_BOX);
        }

        int strHeightHalf = Widget.txtRend.fontHeight / 2;
        int strAreaWidth = Widget.txtRend.getWidth(title) + 4;

        txtRend.draw(ms, title, x + 8, y + 2, this.color);

        DrawableHelper.fill(ms, x + 2, y + 1 + strHeightHalf, strAreaWidth, strHeightHalf, strAreaWidth);

        this.drawHLine(ms, x + 2, y + 1 + strHeightHalf, 4);
        this.drawHLine(ms, x + 2 + strAreaWidth + 4, y + 1 + strHeightHalf, w - 4 - 4 - strAreaWidth);
        this.drawHLine(ms, x + 2, y + h - 2, w - 4);

        this.drawVLine(ms, x + 1, y + 2 + strHeightHalf, h - 4 - strHeightHalf);
        this.drawVLine(ms, x + w - 2, y + 2 + strHeightHalf, h - 4 - strHeightHalf);

        int yOffs = y + BORDER_SZE_N;

        for (Component c : components) {
            c.render(ms, x + BORDER_SZE_W, yOffs);
            yOffs += c.getHeight() + 4;
        }
    }

    private void drawHLine(MatrixStack ms, int xpos, int ypos, int width) {
        DrawableHelper.fill(ms, xpos, ypos, xpos + width, ypos + 1, this.color);
    }

    private void drawVLine(MatrixStack ms, int xpos, int ypos, int height) {
        DrawableHelper.fill(ms, xpos, ypos, xpos + 1, ypos + height, this.color);
    }

    // static final int ICO_DIM = 16;
    // static final int PAD_S = 2;
    // static final int PAD_L = 4;
    // static final int SKIN_ICO_DIM = 8;

    // static final int TEXT_H = txtRend.fontHeight;
    // static final int BAR_H = TEXT_H;

    // static final int TITLED_BAR_H = TEXT_H + PAD_S + BAR_H + PAD_L;
    // static final int ICO_LINE_H = ICO_DIM + PAD_L;

    // public void drawRect(MatrixStack ms, int x, int y, int w, int h) {
    // DrawableHelper.fill(ms, x + xpos + CONTENT_OFFS_X, y + ypos + CONTENT_OFFS_Y,
    // x + xpos + CONTENT_OFFS_X + w,
    // y + ypos + CONTENT_OFFS_Y + h, this.color);
    // }

    // void drawIcon(ItemStack ico, int x, int y) {
    // itmRend.renderGuiItemIcon(ico, x + x + CONTENT_OFFS_X, y + y
    // + CONTENT_OFFS_Y);
    // }

    // void drawText(MatrixStack ms, Text text, int x, int y, int color) {
    // txtRend.draw(ms, text, x + x + CONTENT_OFFS_X, y + y +
    // CONTENT_OFFS_Y, 0xff000000 | color);
    // }

    // void drawText(MatrixStack ms, Text text, int x, int y) {
    // this.drawText(ms, text, x, y, 0xffffffff);
    // }

    // void drawIcoText(MatrixStack ms, ItemStack ico, Text text, int x, int y) {
    // this.drawIcon(ico, x, y);
    // this.drawText(ms, text, x + ICO_DIM + PAD_L, y + 5);
    // }

    // void fill(MatrixStack ms, int x1, int y1, int x2, int y2, int color) {
    // DrawableHelper.fill(ms, x1 + x + CONTENT_OFFS_X, y1 + y +
    // CONTENT_OFFS_Y,
    // x2 + x + CONTENT_OFFS_X, y2 + y + CONTENT_OFFS_Y, 0xff000000
    // | color);
    // }

    // void drawBar(MatrixStack ms, int x, int y, int width, float fillPcnt, int
    // color) {
    // this.fill(ms, x, y, x + width, y + 10, COL_BG_BAR);
    // this.fill(ms, x, y, x + (int) (width * (fillPcnt / 100f)), y + 10, color);
    // }

    // void drawTitledIcoBar(MatrixStack ms, ItemStack ico, Text title, int width,
    // float pcnt, int x, int y, int color) {
    // final int ICO_OFFS = 3;
    // this.drawIcon(ico, x, y + ICO_OFFS);
    // this.drawText(ms, title, x + ICO_DIM + PAD_L, y);
    // this.drawBar(ms, x + ICO_DIM + PAD_L, y + TEXT_H + PAD_S, width, pcnt,
    // color);
    // }

    // void drawIcoFatText(MatrixStack ms, ItemStack ico, Text line1, Text line2,
    // int x, int y) {
    // final int ICO_OFFS = 1;
    // this.drawIcon(ico, x, y + ICO_OFFS);
    // this.drawText(ms, line1, x + ICO_DIM + PAD_L, y);
    // this.drawText(ms, line2, x + ICO_DIM + PAD_L, y + TEXT_H + PAD_S);
    // }

    // public void drawPlayerIco(MatrixStack ms, int x, int y, Identifier
    // skinTexture) {
    // RenderSystem.setShaderTexture(0, skinTexture);
    // PlayerSkinDrawer.draw(ms, x + x + CONTENT_OFFS_X, y + y +
    // CONTENT_OFFS_Y, SKIN_ICO_DIM);
    // }
}
