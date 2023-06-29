package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class TwinClawsIndicator {
    private static long lastDisplayTime = 0;
    public static void updateIce(WorldRenderContext worldRenderContext) {
        if(!SkyblockerConfig.get().slayer.vampireSlayer.enableHolyIceIndicator) return;
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;
        if(!SlayerUtils.getIsInSlayer()) return;

        var client = MinecraftClient.getInstance();

        var slayerEntity = SlayerUtils.GetSlayerEntity();
        if(slayerEntity == null) return;

        for (var entity : SlayerUtils.GetEntityArmorStands(slayerEntity)) {
            if(entity.getDisplayName().toString().contains("TWINCLAWS")) {
                SkyblockerMod.getInstance().scheduler.schedule(() -> {
                    if(System.currentTimeMillis() - lastDisplayTime > 2500) {
                        lastDisplayTime = System.currentTimeMillis();
                        client.inGameHud.setTitleTicks(0, 40, 5);
                        client.inGameHud.setTitle(Text.translatable("skyblocker.rift.iceNow").formatted(Formatting.AQUA));
                        if (client.player != null) {
                            client.player.playSound(SoundEvent.of(new Identifier("minecraft", "entity.experience_orb.pickup")), 100f, 0.1f);
                        }
                    }
                }, SkyblockerConfig.get().slayer.vampireSlayer.holyIceIndicatorTickDelay);
            }
        }

    }
}