package de.hysky.skyblocker.utils.ws;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.ws.message.Message;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.azureaaron.hmapi.data.server.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Optional;

public class WsStateManager {
	private static final ReferenceSet<Service> SUBSCRIBED_SERVICES = new ReferenceOpenHashSet<>();
	private static String lastServerId = "";

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	private static void reset() {
		if (!lastServerId.isEmpty()) {
			for (Service service : SUBSCRIBED_SERVICES) {
				WsMessageHandler.sendSimple(Type.UNSUBSCRIBE, service, lastServerId, Optional.empty());
			}

			lastServerId = "";
		}
	}

	/**
	 * @implNote The service must be registered after the {@link ClientPlayConnectionEvents#JOIN} event fires, one good
	 * place is inside of the {@link SkyblockEvents#LOCATION_CHANGE} event.
	 */
	public static void subscribe(Service service, Optional<Message<? extends Message<?>>> message) {
		if (Utils.getEnvironment() != Environment.PRODUCTION) return;

		SUBSCRIBED_SERVICES.add(service);
		WsMessageHandler.sendSimple(Type.SUBSCRIBE, service, Utils.getServer(), message);

		//Update tracked server id
		lastServerId = Utils.getServer();
	}
}
