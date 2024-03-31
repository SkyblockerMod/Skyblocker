package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.skyblock.FancyStatusBars;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class StatusBarsConfigScreen extends Screen {

    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");

    private @Nullable StatusBar cursorBar = null;
    protected StatusBarsConfigScreen(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(HOTBAR_TEXTURE, width/2 - 91, height-22, 182, 22);
        if (cursorBar != null) {
            cursorBar.setX(mouseX);
            cursorBar.setY(mouseY);
            cursorBar.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void init() {
        super.init();
        FancyStatusBars.statusBars.values().forEach(this::setup);
    }

    private void setup(StatusBar statusBar) {
        this.addDrawableChild(statusBar);
        statusBar.setOnClick(this::onClick);
    }

    private void onClick(StatusBar statusBar) {

    }
}
