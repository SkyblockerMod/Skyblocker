package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.EmptyWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
        return List.of(IntIntMutablePair.of(config.locations.dwarvenMines.crystalsHud.x, config.locations.dwarvenMines.crystalsHud.y));
    }

    @Override
    protected void renderWidget(DrawContext context, List<Widget> widgets) {
        int size = CrystalsHud.getDimensionsForConfig();
        WIDGET.setDimensions(size);
        context.drawTexture(CrystalsHud.MAP_TEXTURE, WIDGET.getX(), WIDGET.getY(), 0, 0, size, size, size, size);
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, List<Widget> widgets) {
        configManager.locations.dwarvenMines.crystalsHud.x = widgets.get(0).getX();
        configManager.locations.dwarvenMines.crystalsHud.y = widgets.get(0).getY();
    }
}
