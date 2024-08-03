package de.hysky.skyblocker.utils.ws;

import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class WsStateManager {
	private static final ReferenceSet<Service> SUBSCRIBED_SERVICES = new ReferenceOpenHashSet<>();
	private static String lastServerId = "";

	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	private static void reset() {
		if (!lastServerId.isEmpty()) {
			for (Service service : SUBSCRIBED_SERVICES) {
				WsMessageHandler.sendSimple(Type.UNSUBSCRIBE, service, lastServerId);
			}

			lastServerId = "";
		}
	}

	/**
	 * @implNote The service must be registered after the {@link ClientPlayConnectionEvents#JOIN} event fires, one good
	 * place is inside of the {@link SkyblockEvents#LOCATION_CHANGE} event.
	 */
	public static void subscribe(Service service) {
		SUBSCRIBED_SERVICES.add(service);
		WsMessageHandler.sendSimple(Type.SUBSCRIBE, service, Utils.getServer());

		//Update tracked server id
		lastServerId = Utils.getServer();
	}
}
