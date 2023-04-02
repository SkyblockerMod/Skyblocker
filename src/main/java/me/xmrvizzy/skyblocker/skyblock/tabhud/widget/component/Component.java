package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Component {

    static final int ICO_DIM = 16;
    static final int PAD_S = 2;
    static final int PAD_L = 4;

    static TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;
    static ItemRenderer itmRend = MinecraftClient.getInstance().getItemRenderer();

    int width, height;

    public abstract void render(MatrixStack ms, int x, int y);

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
