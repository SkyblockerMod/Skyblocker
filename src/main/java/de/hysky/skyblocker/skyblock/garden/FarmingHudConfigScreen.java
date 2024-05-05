package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class FarmingHudConfigScreen extends HudConfigScreen {
    public FarmingHudConfigScreen(Screen parent) {
        super(Text.literal("Farming HUD Config"), parent, FarmingHudWidget.INSTANCE);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
        return List.of(
                IntIntMutablePair.of(config.farming.garden.farmingHud.x, config.farming.garden.farmingHud.y)
        );
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, List<HudWidget> widgets) {
        configManager.farming.garden.farmingHud.x = widgets.getFirst().getX();
        configManager.farming.garden.farmingHud.y = widgets.getFirst().getY();
    }
}
