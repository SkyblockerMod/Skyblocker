package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud.Commission;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudPowderWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class DwarvenHudConfigScreen extends HudConfigScreen {
    private static final List<DwarvenHud.Commission> CFG_COMMS = List.of(new Commission("Test Commission 1", "1%"), new DwarvenHud.Commission("Test Commission 2", "2%"));

    protected DwarvenHudConfigScreen() {
        this(null);
    }

    public DwarvenHudConfigScreen(Screen parent) {
    	super(Text.literal("Dwarven HUD Config"), parent, List.of(HudCommsWidget.INSTANCE_CFG, HudPowderWidget.INSTANCE_CFG));
        if (SkyblockerConfigManager.get().mining.dwarvenHud.style == MiningConfig.DwarvenHudStyle.CLASSIC) {
            HudCommsWidget.INSTANCE_CFG.setWidth(200);
            HudCommsWidget.INSTANCE_CFG.setHeight(20 * CFG_COMMS.size());
            HudPowderWidget.INSTANCE_CFG.setWidth(200);
            HudPowderWidget.INSTANCE_CFG.setHeight(40);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
        return List.of(
                IntIntMutablePair.of(config.mining.dwarvenHud.commissionsX, config.mining.dwarvenHud.commissionsY),
                IntIntMutablePair.of(config.mining.dwarvenHud.powderX, config.mining.dwarvenHud.powderY)
        );
    }

    @Override
    protected void renderWidget(DrawContext context, List<Widget> widgets) {
        DwarvenHud.render(HudCommsWidget.INSTANCE_CFG, HudPowderWidget.INSTANCE_CFG, context, widgets.getFirst().getX(), widgets.getFirst().getY(), widgets.get(1).getX(), widgets.get(1).getY(), CFG_COMMS);
    }

    @Override
    protected void savePos(SkyblockerConfig configManager, List<Widget> widgets) {
        configManager.mining.dwarvenHud.commissionsX = widgets.getFirst().getX();
        configManager.mining.dwarvenHud.commissionsY = widgets.getFirst().getY();
        configManager.mining.dwarvenHud.powderX = widgets.get(1).getX();
        configManager.mining.dwarvenHud.powderY = widgets.get(1).getY();
    }
}
