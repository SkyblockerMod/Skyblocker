package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * A more bare-bones version of Vanilla's Popup Screen. Meant to be extended.
 */
public class AbstractPopupScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("popup/background");
    private final Screen backgroundScreen;

    protected AbstractPopupScreen(Text title, Screen backgroundScreen) {
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
        initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.backgroundScreen.resize(this.client, this.width, this.height);
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        this.backgroundScreen.blur();
    }

    public static class EnterConfirmTextFieldWidget extends TextFieldWidget {

        private final Runnable onEnter;

        public EnterConfirmTextFieldWidget(TextRenderer textRenderer, int width, int height, Text text, Runnable onEnter) {
            this(textRenderer, 0, 0, width, height, text, onEnter);
        }

        public EnterConfirmTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text,Runnable onEnter) {
            this(textRenderer, x, y, width, height, null, text, onEnter);
        }

        public EnterConfirmTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text, Runnable onEnter) {
            super(textRenderer, x, y, width, height, copyFrom, text);
            this.onEnter = onEnter;
        }


        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!super.keyPressed(keyCode, scanCode, modifiers)) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    onEnter.run();
                    return true;
                }
            } else return true;
            return false;
        }
    }

}