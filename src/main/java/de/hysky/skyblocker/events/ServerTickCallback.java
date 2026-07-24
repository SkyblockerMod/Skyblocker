package de.hysky.skyblocker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ServerTickCallback {
	/*
	 * Called upon receiving a ping packet which indicates the server ticked.
	 */
	Event<ServerTickCallback> EVENT = EventFactory.createArrayBacked(ServerTickCallback.class, listeners -> () -> {
		for (ServerTickCallback listener : listeners) {
			listener.onTick();
		}
	});

	void onTick();
}
