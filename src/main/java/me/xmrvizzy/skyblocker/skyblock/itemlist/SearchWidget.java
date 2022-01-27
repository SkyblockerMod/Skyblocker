package me.xmrvizzy.skyblocker.skyblock.itemlist;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SearchWidget extends TextFieldWidget {
    protected final int x;
    protected final int y;
    protected final TextRenderer textRenderer;

    public SearchWidget(TextRenderer textRenderer, int x, int y, int width) {
        super(textRenderer, x + 1, y + 12, width - 2, 16, Text.of("Search"));
        this.x = x;
        this.y = y;
        this.textRenderer = textRenderer;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        this.textRenderer.drawWithShadow(matrices, "Search", this.x, this.y, 0xff9e9e9e);
    }
}
