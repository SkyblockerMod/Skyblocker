package de.hysky.skyblocker.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;

public class ParticleEvents {
	/**
	 * Called upon receiving a {@link ParticleS2CPacket} from the server.
	 */
	public static final Event<FromServer> FROM_SERVER = EventFactory.createArrayBacked(FromServer.class, callbacks -> packet -> {
		for (FromServer callback : callbacks) {
			callback.onParticleFromServer(packet);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface FromServer {
		void onParticleFromServer(ParticleS2CPacket packet);
	}
}
