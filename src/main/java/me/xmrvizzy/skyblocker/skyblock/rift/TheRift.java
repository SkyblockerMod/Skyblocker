package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheRift {
    public static final String LOCATION = "rift";

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MirrorverseWaypoints::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EffigyWaypoints::render);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(StakeIndicator::updateStake, 10);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(TwinClawsIndicator::updateIce, 10);
        EffigyWaypoints.init();
    }
}
