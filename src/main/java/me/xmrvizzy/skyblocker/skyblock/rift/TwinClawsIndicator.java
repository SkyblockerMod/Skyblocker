package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import me.xmrvizzy.skyblocker.utils.render.title.Title;
import me.xmrvizzy.skyblocker.utils.render.title.TitleContainer;
import me.xmrvizzy.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class TwinClawsIndicator {
    private static final Title title = new Title("skyblocker.rift.iceNow", Formatting.AQUA);
    private static boolean scheduled = false;

    protected static void updateIce() {
        if (!SkyblockerConfigManager.get().slayer.vampireSlayer.enableHolyIceIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getLocation().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) {
            TitleContainer.removeTitle(title);
            return;
        }

        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity == null) return;

        boolean anyClaws = false;
        for (Entity entity : SlayerUtils.getEntityArmorStands(slayerEntity)) {
            if (entity.getDisplayName().toString().contains("TWINCLAWS")) {
                anyClaws = true;
                if (!TitleContainer.containsTitle(title) && !scheduled) {
                    scheduled = true;
                    Scheduler.INSTANCE.schedule(() -> {
                        RenderHelper.displayInTitleContainerAndPlaySound(title);
                        scheduled = false;
                    }, SkyblockerConfigManager.get().slayer.vampireSlayer.holyIceIndicatorTickDelay);
                }
            }
        }
        if (!anyClaws) {
            TitleContainer.removeTitle(title);
        }
    }
}