package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

public class DwarvenHudConfigScreen extends Screen {

    private int hudX = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x;
    private int hudY = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y;
    protected DwarvenHudConfigScreen(Text title) {
        super(title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderBackground(matrices);
        DwarvenHud.render(matrices, hudX, hudY, List.of(new DwarvenHud.Commission("Test Commission 1", "1%"), new DwarvenHud.Commission("Test Commission 2", "2%")));
        drawCenteredText(matrices, textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (RenderUtils.pointExistsInArea((int) mouseX, (int) mouseY, hudX, hudY, hudX + 200, hudY + 40) && button == 0) {
            hudX = (int) Math.max(Math.min(mouseX - 100, this.width - 200), 0);
            hudY = (int) Math.max(Math.min(mouseY - 20, this.height - 40), 0);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            hudX = this.width / 2 - 100;
            hudY = this.height / 2 - 20;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x = hudX;
        SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y = hudY;
        AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
        super.close();
    }
}
