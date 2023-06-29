package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class TwinClawsIndicator {
    private static long lastDisplayTime = 0;

    public static void updateIce() {
        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableHolyIceIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getLocation().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) return;

        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity == null) return;

        for (Entity entity : SlayerUtils.getEntityArmorStands(slayerEntity)) {
            if (entity.getDisplayName().toString().contains("TWINCLAWS")) {
                SkyblockerMod.getInstance().scheduler.schedule(() -> {
                    if (System.currentTimeMillis() - lastDisplayTime > 2500) {
                        lastDisplayTime = System.currentTimeMillis();
                        RenderHelper.displayTitleAndPlaySound(40, 5, "skyblocker.rift.iceNow", Formatting.AQUA);
                    }
                }, SkyblockerConfig.get().slayer.vampireSlayer.holyIceIndicatorTickDelay);
            }
        }

    }
}