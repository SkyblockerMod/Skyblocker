package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EndHudConfigScreen extends HudConfigScreen {
    public EndHudConfigScreen(Screen parent) {
        super(Text.literal("End HUD Config"), EndHudWidget.INSTANCE, parent);
    }

    @Override
    protected int[] getPosFromConfig(SkyblockerConfig config) {
        return new int[]{
                config.locations.end.x,
                config.locations.end.y,
        };
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, int x, int y) {
        configManager.locations.end.x = x;
        configManager.locations.end.y = y;
    }

    @Override
    protected void renderWidget(DrawContext context, int x, int y) {
        EndHudWidget.INSTANCE.setX(x);
        EndHudWidget.INSTANCE.setY(y);
        EndHudWidget.INSTANCE.render(context, SkyblockerConfigManager.get().locations.end.enableBackground);
    }
}
