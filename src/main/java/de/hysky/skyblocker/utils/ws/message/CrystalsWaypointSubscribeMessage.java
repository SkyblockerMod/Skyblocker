package de.hysky.skyblocker.utils.ws.message;

import java.time.Duration;
import java.util.Optional;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.slf4j.Logger;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;

import de.hysky.skyblocker.debug.Debug;

/// @param timestamp The time at which the lobby will close in epoch seconds.
public record CrystalsWaypointSubscribeMessage(long timestamp) implements Message<CrystalsWaypointSubscribeMessage> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<CrystalsWaypointSubscribeMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("timestamp").forGetter(CrystalsWaypointSubscribeMessage::timestamp))
			.apply(instance, CrystalsWaypointSubscribeMessage::new));
	/// Crystal Hollows lobbies close after 26 Minecraft days.
	private static final long MAX_LOBBY_LIFETIME = 26;
	private static final long MILLIS_PER_MINECRAFT_DAY = Duration.ofMinutes(20).toMillis();
	private static final long MILLIS_TO_SECONDS = 1000;

	public static CrystalsWaypointSubscribeMessage create(ClientLevel world) {
		ClockManager clockManager = world.clockManager();
		Optional<Holder.Reference<Timeline>> timeline = world.registryAccess()
				.get(Timelines.OVERWORLD_DAY);

		if (timeline.isPresent()) {
			int dayCount = timeline.get().value().getPeriodCount(clockManager);

			// Makes it easy to debug day count related problems
			if (Debug.debugEnabled() && Debug.webSocketDebug()) {
				LOGGER.info("[Skyblocker WebSocket] CH Day Count: {}", dayCount);
			}

			// Require the day count to be in the range [0, 26)
			if (dayCount >= 0 && dayCount < MAX_LOBBY_LIFETIME) {
				long closeTime = System.currentTimeMillis() + ((MAX_LOBBY_LIFETIME - dayCount) * MILLIS_PER_MINECRAFT_DAY);

				return new CrystalsWaypointSubscribeMessage(closeTime / MILLIS_TO_SECONDS);
			}
		}

		// If the timeline isn't present (for whatever weird reason) then just default to sending a close date 26 mc days later.
		long defaultCloseTime = System.currentTimeMillis() + (MAX_LOBBY_LIFETIME * MILLIS_PER_MINECRAFT_DAY);
		LOGGER.warn("[Skyblocker WebSocket] Falling back to default CH close time of {} due to unavailable timeline.", defaultCloseTime);

		return new CrystalsWaypointSubscribeMessage(defaultCloseTime / MILLIS_TO_SECONDS);
	}

	@Override
	public Codec<CrystalsWaypointSubscribeMessage> getCodec() {
		return CODEC;
	}
}
