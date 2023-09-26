package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import me.xmrvizzy.skyblocker.utils.render.title.Title;
import me.xmrvizzy.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

public class HealingMelonIndicator {
    private static final Title title = new Title("skyblocker.rift.healNow", Formatting.DARK_RED);

    public static void updateHealth() {
        if (!SkyblockerConfigManager.get().slayer.vampireSlayer.enableHealingMelonIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château")) {
            TitleContainer.removeTitle(title);
            return;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getHealth() <= SkyblockerConfigManager.get().slayer.vampireSlayer.healingMelonHealthThreshold * 2F) {
            RenderHelper.displayInTitleContainerAndPlaySound(title);
        } else {
            TitleContainer.removeTitle(title);
        }
    }
}