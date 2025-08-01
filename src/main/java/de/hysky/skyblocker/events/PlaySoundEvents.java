package de.hysky.skyblocker.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;

public class PlaySoundEvents {
	public static final Event<FromServer> FROM_SERVER = EventFactory.createArrayBacked(FromServer.class, callbacks -> packet -> {
		for (FromServer callback : callbacks) {
			callback.onPlaySoundFromServer(packet);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface FromServer {
		void onPlaySoundFromServer(PlaySoundS2CPacket packet);
	}
}
