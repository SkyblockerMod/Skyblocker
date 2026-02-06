package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class HealingMelonIndicator {
	private static final Title title = new Title("skyblocker.rift.healNow", ChatFormatting.DARK_RED);

	public static void updateHealth() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableHealingMelonIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || Utils.getArea() != Area.TheRift.CHATEAU) {
			TitleContainer.removeTitle(title);
			return;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && player.getHealth() <= SkyblockerConfigManager.get().slayers.vampireSlayer.healingMelonHealthThreshold * 2F) {
			TitleContainer.addTitleAndPlaySound(title);
		} else {
			TitleContainer.removeTitle(title);
		}
	}
}
