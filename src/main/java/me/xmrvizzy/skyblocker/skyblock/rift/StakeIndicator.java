package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
public class StakeIndicator {
    private static long lastDisplayTime = 0;
    public static void UpdateStake(WorldRenderContext worldRenderContext) {
        if(!SkyblockerConfig.get().slayer.vampireSlayer.enableSteakStakeIndicator) return;
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;

        var client = MinecraftClient.getInstance();

        if(!SlayerUtils.getIsInSlayer()) return;
        var slayerEntity = SlayerUtils.GetSlayerEntity();

        if(slayerEntity != null) {
            if(slayerEntity.getDisplayName().toString().contains("҉")) {
                if (System.currentTimeMillis() - lastDisplayTime > 2500) {
                    lastDisplayTime = System.currentTimeMillis();
                    client.inGameHud.setTitleTicks(0, 25, 5);
                    client.inGameHud.setTitle(Text.translatable("skyblocker.rift.stakeNow").formatted(Formatting.RED));
                    if (client.player != null) {
                        client.player.playSound(SoundEvent.of(new Identifier("minecraft", "entity.experience_orb.pickup")), 100f, 0.1f);
                    }
                }
            }
        }
    }
}