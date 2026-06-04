package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;

public sealed interface Message<T extends Message<T>> permits CrystalsWaypointMessage, CrystalsWaypointSubscribeMessage, DungeonMimicKilledMessage, DungeonPrinceKilledMessage, DungeonRoomHideWaypointMessage, DungeonRoomMatchMessage, DungeonRoomSecretCountMessage, EggWaypointMessage {

	Codec<T> getCodec();
}
