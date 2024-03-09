package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudFarmingWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class FarmingHudConfigScreen extends HudConfigScreen {
    public FarmingHudConfigScreen(Screen parent) {
        super(Text.literal("Farming HUD Config"), parent, HudFarmingWidget.INSTANCE);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
        return List.of(
                IntIntMutablePair.of(config.locations.garden.farmingHud.x, config.locations.garden.farmingHud.y)
        );
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, List<Widget> widgets) {
        configManager.locations.garden.farmingHud.x = widgets.get(0).getX();
        configManager.locations.garden.farmingHud.y = widgets.get(0).getY();
    }
}
