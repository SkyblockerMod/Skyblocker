package me.xmrvizzy.skyblocker.config.hud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class HudConfig extends Screen {
    protected HudConfig() {
        super(Text.of("HUD Config"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
    }

    @Override
    protected void init() {
        super.init();
    }
}
