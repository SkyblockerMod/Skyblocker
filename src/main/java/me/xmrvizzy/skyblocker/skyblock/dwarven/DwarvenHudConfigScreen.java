package me.xmrvizzy.skyblocker.skyblock.dwarven;

import java.awt.Color;
import java.util.List;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud.Commission;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public class DwarvenHudConfigScreen extends Screen {

    private static final List<Commission> CFG_COMMS = List.of(new DwarvenHud.Commission("Test Commission 1", "1%"), new DwarvenHud.Commission("Test Commission 2", "2%"));

    private int hudX = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x;
    private int hudY = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y;

    protected DwarvenHudConfigScreen() {
        super(Text.of("Dwarven HUD Config"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context);
        DwarvenHud.render(HudCommsWidget.INSTANCE_CFG, context, hudX, hudY, List.of(new DwarvenHud.Commission("Test Commission 1", "1%"), new DwarvenHud.Commission("Test Commission 2", "2%")));
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Pair<Integer, Integer> dims = DwarvenHud.getDimForConfig(CFG_COMMS);
        if (RenderUtils.pointExistsInArea((int) mouseX, (int) mouseY, hudX, hudY, hudX + 200, hudY + 40) && button == 0) {
            hudX = (int) Math.max(Math.min(mouseX - dims.getLeft()/2, this.width - dims.getLeft()), 0);
            hudY = (int) Math.max(Math.min(mouseY - dims.getRight()/2, this.height - dims.getRight()), 0);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            Pair<Integer, Integer> dims = DwarvenHud.getDimForConfig(CFG_COMMS);
            hudX = this.width / 2 - dims.getLeft();
            hudY = this.height / 2 - dims.getRight();
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
