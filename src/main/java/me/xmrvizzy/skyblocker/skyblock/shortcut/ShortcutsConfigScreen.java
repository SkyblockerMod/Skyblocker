package me.xmrvizzy.skyblocker.skyblock.shortcut;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ShortcutsConfigScreen extends Screen {
    public ShortcutsConfigScreen() {
        super(Text.translatable("skyblocker.shortcuts.config"));
    }

    @Override
    protected void init() {
        super.init();
        addDrawableChild(new ShortcutsConfigListWidget(client, width, height, 32, height - 32, 25));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
    }
}
