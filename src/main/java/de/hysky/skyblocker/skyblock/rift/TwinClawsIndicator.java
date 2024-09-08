package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class TwinClawsIndicator {
    private static final Title title = new Title("skyblocker.rift.iceNow", Formatting.AQUA);
    private static boolean scheduled = false;

    protected static void updateIce() {
        if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableHolyIceIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getIslandArea().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) {
            TitleContainer.removeTitle(title);
            return;
        }

        Entity slayerEntity = SlayerUtils.getSlayerArmorStandEntity();
        if (slayerEntity == null) return;

        boolean anyClaws = false;
        for (Entity entity : SlayerUtils.getEntityArmorStands(slayerEntity, 2.5f)) {
            if (entity.getDisplayName().toString().contains("TWINCLAWS")) {
                anyClaws = true;
                if (!TitleContainer.containsTitle(title) && !scheduled) {
                    scheduled = true;
                    Scheduler.INSTANCE.schedule(() -> {
                        RenderHelper.displayInTitleContainerAndPlaySound(title);
                        scheduled = false;
                    }, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceIndicatorTickDelay);
                }
            }
        }
        if (!anyClaws) {
            TitleContainer.removeTitle(title);
        }
    }
}
