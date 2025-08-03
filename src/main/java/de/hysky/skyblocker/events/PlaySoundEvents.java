package de.hysky.skyblocker.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;

public class PlaySoundEvents {
	/**
	 * Called upon receiving a {@link PlaySoundS2CPacket} from the server.
	 *
	 * @implNote This event always fires regardless of any other events.
	 */
	public static final Event<FromServer> FROM_SERVER = EventFactory.createArrayBacked(FromServer.class, callbacks -> packet -> {
		for (FromServer callback : callbacks) {
			callback.onPlaySoundFromServer(packet);
		}
	});
	/**
	 * Called to determine whether a {@code sound} should be played or not.
	 */
	public static final Event<AllowSound> ALLOW_SOUND = EventFactory.createArrayBacked(AllowSound.class, callbacks -> sound -> {
		boolean allowSound = true;

		for (AllowSound callback : callbacks) {
			allowSound &= callback.allowSound(sound);
		}

		return allowSound;
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface FromServer {
		void onPlaySoundFromServer(PlaySoundS2CPacket packet);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AllowSound {
		boolean allowSound(SoundEvent sound);
	}
}
