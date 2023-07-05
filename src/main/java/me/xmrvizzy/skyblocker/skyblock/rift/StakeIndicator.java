package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class StakeIndicator {
    private static Title title = null;
    public static void updateStake() {
        if (title == null)
            title = new Title("b", Formatting.RED.getColorValue());

        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableSteakStakeIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château") || !SlayerUtils.isInSlayer()) {
            title.active = false;
            return;
        }
        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity != null && slayerEntity.getDisplayName().toString().contains("҉")) {
            title.active = true;
            title.text = I18n.translate("skyblocker.rift.stakeNow");
            if(!TitleContainer.titles.contains(title))
                RenderHelper.displayInTitleContainerAndPlaySound(title);
        }
        else
            title.active = false;
    }
}