package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TwinClawsIndicator {
    private static final Title title = new Title("skyblocker.rift.iceNow",Formatting.AQUA);
    private static boolean scheduled = false;

    protected static void updateIce() {
        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableHolyIceIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getLocation().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) {
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
                    SkyblockerMod.getInstance().scheduler.schedule(() -> {
                        RenderHelper.displayInTitleContainerAndPlaySound(title);
                        scheduled = false;
                    }, SkyblockerConfig.get().slayer.vampireSlayer.holyIceIndicatorTickDelay);
                }
            }
        }
        if (!anyClaws) {
            TitleContainer.removeTitle(title);
        }
    }
}