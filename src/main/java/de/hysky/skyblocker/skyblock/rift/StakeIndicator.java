package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class StakeIndicator {
    private static final Title title = new Title("skyblocker.rift.stakeNow", Formatting.RED);

    protected static void updateStake() {
        if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableSteakStakeIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getIslandArea().contains("Stillgore Château") || !SlayerUtils.isInSlayer()) {
            TitleContainer.removeTitle(title);
            return;
        }
        Entity slayerEntity = SlayerUtils.getSlayerArmorStandEntity();
        if (slayerEntity != null && slayerEntity.getDisplayName().toString().contains("҉")) {
            RenderHelper.displayInTitleContainerAndPlaySound(title);
        } else {
            TitleContainer.removeTitle(title);
        }
    }
}
