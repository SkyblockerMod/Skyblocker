package de.hysky.skyblocker.skyblock.slayers.boss.vampire;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;

public class StakeIndicator {
	private static final Title title = new Title("skyblocker.rift.stakeNow", ChatFormatting.RED);

	public static void updateStake() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableSteakStakeIndicator || !SlayerManager.isFightingSlayerType(SlayerType.VAMPIRE)) {
			TitleContainer.removeTitle(title);
			return;
		}
		Entity slayerEntity = SlayerManager.getSlayerArmorStand();
		if (slayerEntity != null && slayerEntity.getName().toString().contains("҉")) {
			TitleContainer.addTitleAndPlaySound(title);
		} else {
			TitleContainer.removeTitle(title);
		}
	}
}
