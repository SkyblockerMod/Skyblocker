package de.hysky.skyblocker.skyblock.slayers.boss.vampire;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class StakeIndicator {
    private static final Title title = new Title("skyblocker.rift.stakeNow", Formatting.RED);

	public static void updateStake() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableSteakStakeIndicator || !SlayerManager.isInSlayerType(SlayerType.VAMPIRE)) {
            TitleContainer.removeTitle(title);
            return;
        }
		Entity slayerEntity = SlayerManager.getSlayerBossArmorStand();
        if (slayerEntity != null && slayerEntity.getDisplayName().toString().contains("҉")) {
            TitleContainer.addTitleAndPlaySound(title);
        } else {
            TitleContainer.removeTitle(title);
        }
    }
}
