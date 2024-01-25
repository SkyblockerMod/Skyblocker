package de.hysky.skyblocker.skyblock.crimson.kuudra;

import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class Kuudra {
	public static final String LOCATION = "kuudra";

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(KuudraWaypoints::render);
		ClientLifecycleEvents.CLIENT_STARTED.register(KuudraWaypoints::load);
		Scheduler.INSTANCE.scheduleCyclic(KuudraWaypoints::tick, 20);
	}
}
