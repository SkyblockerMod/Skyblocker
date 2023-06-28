package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StakeIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StakeIndicator.class);

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(StakeIndicator::UpdateStake);
    }
    private static long lastDisplayTime = 0;
    public static void UpdateStake(MinecraftClient client) {
        if(!SkyblockerConfig.get().slayer.vampireSlayer.enableSteakStakeIndicator) return;
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;
        if(!SlayerUtils.getIsInSlayer()) return;
        var slayerEntity = SlayerUtils.GetSlayerEntity();
        if(slayerEntity != null) {
            LOGGER.info(slayerEntity.getDisplayName().toString());
            if(slayerEntity.getDisplayName().toString().contains("҉"))
            {
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
