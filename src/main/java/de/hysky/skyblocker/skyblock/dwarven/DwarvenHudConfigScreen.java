package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud.Commission;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudPowderWidget;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

public class DwarvenHudConfigScreen extends Screen {

    private static final List<DwarvenHud.Commission> CFG_COMMS = List.of(new Commission("Test Commission 1", "1%"), new DwarvenHud.Commission("Test Commission 2", "2%"));
    private int commissionsHudX = SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.x;
    private int commissionsHudY = SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.y;

    private int powderHudX = SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.powderX;
    private int powderHudY = SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.powderY;
    private final Screen parent;

    protected DwarvenHudConfigScreen() {
        this(null);
    }

    public DwarvenHudConfigScreen(Screen parent) {
    	super(Text.of("Dwarven HUD Config"));
    	this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context, mouseX, mouseY, delta);
        DwarvenHud.render(HudCommsWidget.INSTANCE_CFG, HudPowderWidget.INSTANCE_CFG, context, commissionsHudX, commissionsHudY, powderHudX, powderHudY, CFG_COMMS);
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Pair<IntIntPair,IntIntPair> dims = DwarvenHud.getDimForConfig(CFG_COMMS);
        if (RenderHelper.pointIsInArea(mouseX, mouseY, commissionsHudX, commissionsHudY, commissionsHudX + 200, commissionsHudY + 40) && button == 0) {
            commissionsHudX = (int) Math.max(Math.min(mouseX - (double) dims.first().leftInt() / 2, this.width - dims.first().leftInt()), 0);
            commissionsHudY = (int) Math.max(Math.min(mouseY - (double) dims.first().rightInt() / 2, this.height - dims.first().rightInt()), 0);
        }
        if (RenderHelper.pointIsInArea(mouseX, mouseY, powderHudX, powderHudY, powderHudX + 200, powderHudY + 40) && button == 0) {
            powderHudX = (int) Math.max(Math.min(mouseX - (double) dims.second().leftInt() / 2, this.width - dims.second().leftInt()), 0);
            powderHudY = (int) Math.max(Math.min(mouseY - (double) dims.second().rightInt() / 2, this.height - dims.second().rightInt()), 0);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            Pair<IntIntPair,IntIntPair> dims = DwarvenHud.getDimForConfig(CFG_COMMS);
            commissionsHudX = this.width / 2 - dims.left().leftInt();
            commissionsHudY = this.height / 2 - dims.left().rightInt();
            powderHudX = this.width / 2 - dims.right().leftInt();
            powderHudY = this.height / 2 - dims.right().rightInt() + dims.left().rightInt(); //add this to make it bellow the other widget
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.x = commissionsHudX;
        SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.y = commissionsHudY;
        SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.powderX = powderHudX;
        SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.powderY = powderHudY;
        SkyblockerConfigManager.save();

        client.setScreen(parent);
    }
}
