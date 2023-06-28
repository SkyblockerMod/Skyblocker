package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class HealingMelonIndicator {

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(HealingMelonIndicator::UpdateHealth);
    }
    private static long lastDisplayTime = 0;
    public static void UpdateHealth(MinecraftClient client) {
        if(!SkyblockerConfig.get().slayer.vampireSlayer.enableHealingMelonIndicator) return;
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;
        var playerEntity = client.player;
        if(playerEntity != null) {
            //4.5 is 3 hearts for some reason
            if (playerEntity.getHealth() < 7F) {
                if(System.currentTimeMillis() - lastDisplayTime > 2500)
                {
                    lastDisplayTime = System.currentTimeMillis();
                    client.inGameHud.setTitleTicks(0, 15, 5);
                    client.inGameHud.setTitle(Text.translatable("skyblocker.rift.healNow").formatted(Formatting.DARK_RED));
                    playerEntity.playSound(SoundEvent.of(new Identifier("minecraft", "entity.experience_orb.pickup")), 100f, 0.1f);
                }
            }
        }
    }
}
