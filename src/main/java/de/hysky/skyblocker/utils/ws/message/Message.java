package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;

public sealed interface Message<T extends Message<T>> permits CrystalsWaypointMessage, CrystalsWaypointSubscribeMessage {

	Codec<T> getCodec();
}
