package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwinClawsIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwinClawsIndicator.class);
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TwinClawsIndicator::updateIce);
    }
    private static long lastDisplayTime = 0;
    public static void updateIce(MinecraftClient client)
    {
        if(!SkyblockerConfig.get().slayer.vampireSlayer.enableHolyIceIndicator) return;
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;
        if(!SlayerUtils.getIsInSlayer()) return;
        var slayerEntity = SlayerUtils.GetSlayerEntity();
        if(slayerEntity == null) return;
        //TODO: Cache this, probably included in Packet system
        for (var entity : slayerEntity.getEntityWorld().getOtherEntities(slayerEntity, slayerEntity.getBoundingBox().expand(1F, 2.5F, 1F), x-> x instanceof ArmorStandEntity && x.hasCustomName())) {

            LOGGER.info(entity.getDisplayName().toString());
            if(entity.getDisplayName().toString().contains("TWINCLAWS"))
            {
                if(System.currentTimeMillis() - lastDisplayTime > 2500)
                {
                    lastDisplayTime = System.currentTimeMillis();
                    client.inGameHud.setTitleTicks(0, 40, 5);
                    client.inGameHud.setTitle(Text.translatable("skyblocker.rift.iceNow").formatted(Formatting.AQUA));
                    client.player.playSound(SoundEvent.of(new Identifier("minecraft", "entity.experience_orb.pickup")), 100f, 0.1f);
                }
            }
        }

    }
}
