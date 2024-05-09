package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class EndHudConfigScreen extends HudConfigScreen {
    public EndHudConfigScreen(Screen parent) {
        super(Text.literal("End HUD Config"), parent, EndHudWidget.INSTANCE);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
        return List.of(IntIntMutablePair.of(config.otherLocations.end.x, config.otherLocations.end.y));
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, List<Widget> widgets) {
        configManager.otherLocations.end.x = widgets.getFirst().getX();
        configManager.otherLocations.end.y = widgets.getFirst().getY();
    }
}
