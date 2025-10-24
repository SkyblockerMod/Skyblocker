package de.hysky.skyblocker.utils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public interface Resettable extends ClientPlayConnectionEvents.Join {
	void reset();

	@Override
	default void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
		reset();
	}
}
