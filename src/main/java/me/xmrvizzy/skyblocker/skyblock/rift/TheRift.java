package me.xmrvizzy.skyblocker.skyblock.rift;

import dev.architectury.event.events.common.PlayerEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheRift {
	public static final String LOCATION = "rift";

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(MirrorverseWaypoints::render);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EffigyWaypoints::render);
		WorldRenderEvents.END.register(TwinClawsIndicator::updateIce);
		WorldRenderEvents.END.register(StakeIndicator::UpdateStake);
		EffigyWaypoints.init();
	}
}
