package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class StakeIndicator {
    private static long lastDisplayTime = 0;

    public static void updateStake() {
        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableSteakStakeIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château") || !SlayerUtils.isInSlayer()) return;
        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity != null && slayerEntity.getDisplayName().toString().contains("҉") && System.currentTimeMillis() - lastDisplayTime > 2500) {
            lastDisplayTime = System.currentTimeMillis();
            RenderHelper.displayTitleAndPlaySound(25, 5, "skyblocker.rift.stakeNow", Formatting.RED);
        }
    }
}