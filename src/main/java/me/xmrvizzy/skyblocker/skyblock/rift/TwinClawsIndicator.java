package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TwinClawsIndicator {
    private static Title title = null;
    public static boolean scheduling = false;
    public static void updateIce() {
        if(title == null)
            title = new Title("b", Formatting.AQUA.getColorValue());

        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableHolyIceIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getLocation().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) {
            title.active = false;
            return;
        }

        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity == null) return;

        boolean anyClaws = false;
        for (Entity entity : SlayerUtils.getEntityArmorStands(slayerEntity)) {
            if (entity.getDisplayName().toString().contains("TWINCLAWS")) {
                anyClaws = true;
                title.active = true;
                if(!TitleContainer.titles.contains(title) && !scheduling) {
                    scheduling = true;
                    SkyblockerMod.getInstance().scheduler.schedule(() -> {
                        title.text = I18n.translate("skyblocker.rift.iceNow");
                        RenderHelper.displayInTitleContainerAndPlaySound(title);
                        scheduling = false;
                    }, SkyblockerConfig.get().slayer.vampireSlayer.holyIceIndicatorTickDelay);
                }
            }
        }
        if(!anyClaws)
            title.active = false;
    }
}