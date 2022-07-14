package me.xmrvizzy.skyblocker.skyblock.dwarven.hud;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.config.hud.HudConfig;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class DwarvenHudConfig extends HudConfig {
    public static int hudX;
    public static int hudY;
    public static List<DwarvenHud.Commission> fakeCommissions = List.of(new DwarvenHud.Commission("Commission 1", "0%"), new DwarvenHud.Commission("Commission 2", "0%"));

    private boolean shouldFollow = false;

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (RenderUtils.pointExistsInArea(mouseX, mouseY, hudX, hudY, hudX + 200, hudY + (20 * fakeCommissions.size()))) { shouldFollow = true; }
        if (shouldFollow) {
            hudX = (int) mouseX;
            hudY = (int) mouseY;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        shouldFollow = false;
        SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x = hudX;
        SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y = hudY;
        AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        DwarvenHud.drawHud(matrices, fakeCommissions, hudX, hudY);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        hudX = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x;
        hudY = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y;
        super.init();
    }
}
