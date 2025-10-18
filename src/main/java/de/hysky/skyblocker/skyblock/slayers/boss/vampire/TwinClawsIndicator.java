package de.hysky.skyblocker.skyblock.slayers.boss.vampire;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class TwinClawsIndicator {
    private static final Title title = new Title("skyblocker.rift.iceNow", Formatting.AQUA);
    private static boolean scheduled = false;

	public static void updateIce() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableHolyIceIndicator || !SlayerManager.isInSlayerType(SlayerType.VAMPIRE)) {
            TitleContainer.removeTitle(title);
            return;
        }

		Entity slayerEntity = SlayerManager.getSlayerBossArmorStand();
        if (slayerEntity == null) return;

        boolean anyClaws = false;
		for (Entity entity : SlayerManager.getEntityArmorStands(slayerEntity, 2.5f)) {
            if (entity.getDisplayName().toString().contains("TWINCLAWS")) {
                anyClaws = true;
                if (!TitleContainer.containsTitle(title) && !scheduled) {
                    scheduled = true;
                    Scheduler.INSTANCE.schedule(() -> {
                        TitleContainer.addTitleAndPlaySound(title);
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
