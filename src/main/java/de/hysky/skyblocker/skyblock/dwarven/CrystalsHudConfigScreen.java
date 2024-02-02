package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;

public class CrystalsHudConfigScreen extends Screen {

    private int hudX = SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.x;
    private int hudY = SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.y;
    private final Screen parent;

    protected CrystalsHudConfigScreen() {
        this(null);
    }

    public CrystalsHudConfigScreen(Screen parent) {
        super(Text.of("Crystals HUD Config"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context, mouseX, mouseY, delta);
        renderHUDMap(context, hudX, hudY);
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        IntIntPair dims = CrystalsHud.getDimensionsForConfig();
        if (RenderHelper.pointIsInArea(mouseX, mouseY, hudX, hudY, hudX + dims.leftInt(), hudY + dims.rightInt()) && button == 0) {
            hudX = (int) Math.max(Math.min(mouseX - (double) dims.leftInt() / 2, this.width - dims.leftInt()), 0);
            hudY = (int) Math.max(Math.min(mouseY - (double) dims.rightInt() / 2, this.height - dims.rightInt()), 0);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            IntIntPair dims = CrystalsHud.getDimensionsForConfig();
            hudX = this.width / 2 - dims.leftInt();
            hudY = this.height / 2 - dims.rightInt();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderHUDMap(DrawContext context, int x, int y) {
        float scaling = SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.mapScaling;
        int size = (int) (62 * scaling);
        context.drawTexture(CrystalsHud.MAP_TEXTURE, x, y, 0, 0, size, size, size, size);
	}

    @Override
    public void close() {
        SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.x = hudX;
        SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.y = hudY;
        SkyblockerConfigManager.save();

        client.setScreen(parent);
    }
}
