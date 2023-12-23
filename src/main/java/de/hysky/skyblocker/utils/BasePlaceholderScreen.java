package de.hysky.skyblocker.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class BasePlaceholderScreen extends Screen {

    public BasePlaceholderScreen(Text title) {
        super(title);
    }

    @Override
    public void render(final DrawContext guiGraphics, final int i, final int j, final float f) {
    }

    @Override
    public void renderBackground(final DrawContext guiGraphics, final int i, final int j, final float f) {
    }
}
