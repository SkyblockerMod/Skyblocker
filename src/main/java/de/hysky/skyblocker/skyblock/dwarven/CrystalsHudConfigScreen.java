package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.EmptyWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

import java.util.List;

public class CrystalsHudConfigScreen extends HudConfigScreen {
    private static final EmptyWidget WIDGET = new EmptyWidget();

    protected CrystalsHudConfigScreen() {
        this(null);
    }

    public CrystalsHudConfigScreen(Screen parent) {
        super(Text.of("Crystals HUD Config"), parent, WIDGET);
        WIDGET.setDimensions(CrystalsHud.getDimensionsForConfig());
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
        return List.of(IntIntMutablePair.of(config.mining.crystalsHud.x, config.mining.crystalsHud.y));
    }

    @Override
    protected void renderWidget(DrawContext context, List<Widget> widgets) {
        int size = CrystalsHud.getDimensionsForConfig();
        WIDGET.setDimensions(size);
        context.drawTexture(RenderLayer::getGuiTextured, CrystalsHud.MAP_TEXTURE, WIDGET.getX(), WIDGET.getY(), 0, 0, size, size, size, size);
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, List<Widget> widgets) {
        configManager.mining.crystalsHud.x = widgets.getFirst().getX();
        configManager.mining.crystalsHud.y = widgets.getFirst().getY();
    }
}
