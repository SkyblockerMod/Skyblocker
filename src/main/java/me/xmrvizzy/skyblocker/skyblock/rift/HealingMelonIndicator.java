package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

public class HealingMelonIndicator {
    private static final Title title = new Title("skyblocker.rift.healNow", Formatting.DARK_RED);

    public static void updateHealth(MinecraftClient client) {
        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableHealingMelonIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château")) {
            TitleContainer.removeTitle(title);
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player != null && player.getHealth() <= SkyblockerConfig.get().slayer.vampireSlayer.healingMelonHealthThreshold * 2F) {
            RenderHelper.displayInTitleContainerAndPlaySound(title);
        } else {
            TitleContainer.removeTitle(title);
        }
    }
}