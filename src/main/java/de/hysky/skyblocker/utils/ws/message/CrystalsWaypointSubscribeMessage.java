package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.world.ClientWorld;

public record CrystalsWaypointSubscribeMessage(long timestamp) implements Message<CrystalsWaypointSubscribeMessage> {
	private static final Codec<CrystalsWaypointSubscribeMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.LONG.fieldOf("timestamp").forGetter(CrystalsWaypointSubscribeMessage::timestamp))
			.apply(instance, CrystalsWaypointSubscribeMessage::new));
	/**
	 * 26 Minecraft days in ticks
	 */
	private static final int TWENTY_SIX_DAYS = 24000 * 26;

	public static CrystalsWaypointSubscribeMessage create(ClientWorld world) {
		//Current timestamp as seconds + ((26 mc days - mc time) / ticks per second)
		long closeTime = (System.currentTimeMillis() / 1000L) + ((TWENTY_SIX_DAYS - world.getTimeOfDay()) / 20L);

		return new CrystalsWaypointSubscribeMessage(closeTime);
	}

	@Override
	public Codec<CrystalsWaypointSubscribeMessage> getCodec() {
		return CODEC;
	}
}
