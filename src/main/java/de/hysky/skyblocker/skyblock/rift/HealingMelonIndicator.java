package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

public class HealingMelonIndicator {
    private static final Title title = new Title("skyblocker.rift.healNow", Formatting.DARK_RED);

    public static void updateHealth() {
        if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableHealingMelonIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getIslandArea().contains("Stillgore Château")) {
            TitleContainer.removeTitle(title);
            return;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getHealth() <= SkyblockerConfigManager.get().slayers.vampireSlayer.healingMelonHealthThreshold * 2F) {
            RenderHelper.displayInTitleContainerAndPlaySound(title);
        } else {
            TitleContainer.removeTitle(title);
        }
    }
}
