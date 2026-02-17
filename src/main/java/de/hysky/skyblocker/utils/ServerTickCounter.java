package de.hysky.skyblocker.utils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.ServerTickCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.protocol.common.ClientboundPingPacket;

public class ServerTickCounter {
	private static int lastId = -1;

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
	}

	public static void onServerTick(ClientboundPingPacket packet) {
		if (packet.getId() != lastId) {
			lastId = packet.getId();

			ServerTickCallback.EVENT.invoker().onTick();
		}
	}

	private static void reset() {
		lastId = -1;
	}
}
