package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudFarmingWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class FarmingHud {
    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!SkyblockerConfigManager.get().locations.garden.farmingHud.enableHud || Utils.getLocation() != Location.GARDEN) return;
            HudFarmingWidget.INSTANCE.render(context, SkyblockerConfigManager.get().general.tabHud.enableHudBackground);
        });
    }
}
