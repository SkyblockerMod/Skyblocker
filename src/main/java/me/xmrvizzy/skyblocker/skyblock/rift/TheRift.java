package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheRift {
    /**
     * @see me.xmrvizzy.skyblocker.utils.Utils#isInTheRift() Utils#isInTheRift().
     */
    public static final String LOCATION = "rift";

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MirrorverseWaypoints::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EffigyWaypoints::render);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(EffigyWaypoints::updateEffigies, SkyblockerConfig.get().slayer.vampireSlayer.effigyUpdateFrequency);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfig.get().slayer.vampireSlayer.holyIceUpdateFrequency);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfig.get().slayer.vampireSlayer.maniaUpdateFrequency);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfig.get().slayer.vampireSlayer.steakStakeUpdateFrequency);
    }
}
