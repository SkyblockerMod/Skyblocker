package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import me.xmrvizzy.skyblocker.utils.render.title.Title;
import me.xmrvizzy.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class StakeIndicator {
    private static final Title title = new Title("skyblocker.rift.stakeNow", Formatting.RED);

    protected static void updateStake() {
        if (!SkyblockerConfigManager.get().slayer.vampireSlayer.enableSteakStakeIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château") || !SlayerUtils.isInSlayer()) {
            TitleContainer.removeTitle(title);
            return;
        }
        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity != null && slayerEntity.getDisplayName().toString().contains("҉")) {
            RenderHelper.displayInTitleContainerAndPlaySound(title);
        } else {
            TitleContainer.removeTitle(title);
        }
    }
}