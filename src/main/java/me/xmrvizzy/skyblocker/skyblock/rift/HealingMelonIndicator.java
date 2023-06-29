package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

public class HealingMelonIndicator {
    private static long lastDisplayTime = 0;

    public static void UpdateHealth(MinecraftClient client) {
        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableHealingMelonIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château")) return;
        ClientPlayerEntity player = client.player;
        if (player != null && player.getHealth() <= SkyblockerConfig.get().slayer.vampireSlayer.healingMelonHealthThreshold * 2F && System.currentTimeMillis() - lastDisplayTime > 2500) {
            lastDisplayTime = System.currentTimeMillis();
            RenderHelper.displayTitleAndPlaySound(15, 5, "skyblocker.rift.healNow", Formatting.DARK_RED);
        }
    }
}