package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A more bare-bones version of Vanilla's Popup Screen. Meant to be extended.
 */
public class BarebonesPopupScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("popup/background");
    private final Screen backgroundScreen;

    protected BarebonesPopupScreen(Text title, Screen backgroundScreen) {
        super(title);
        this.backgroundScreen = backgroundScreen;
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.backgroundScreen);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.backgroundScreen.render(context, -1, -1, delta);
        context.draw();
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        this.renderInGameBackground(context);
    }

    /**
     * These are the inner positions and size of the popup, not outer
     */
    public static void drawPopupBackground(DrawContext context, int x, int y, int width, int height) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x - 18, y - 18, width + 36, height + 36);
    }

    @Override
    protected void init() {
        super.init();
        this.backgroundScreen.resize(this.client, width, height);

    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        this.backgroundScreen.blur();
    }
}