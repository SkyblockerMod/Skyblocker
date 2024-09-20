package de.hysky.skyblocker.utils.ws;

import java.util.Optional;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.ws.message.CrystalsWaypointMessage;
import de.hysky.skyblocker.utils.ws.message.Message;

public class WsMessageHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * Used for sending messages to the current channel/server
	 */
	public static void sendMessage(Service service, Message<? extends Message<?>> message) {
		send(Type.PUBLISH, service, Utils.getServer(), Optional.of(encodeMessage(message)));
	}

	/**
	 * Useful for sending simple state updates with an optional message
	 */
	static void sendSimple(Type type, Service service, String serverId, Optional<Message<? extends Message<?>>> message) {
		send(type, service, serverId, message.map(WsMessageHandler::encodeMessage));
	}

	private static void send(Type type, Service service, String serverId, Optional<Dynamic<?>> message) {
		try {
			Payload payload = new Payload(type, service, serverId, message);
			JsonObject encoded = Payload.CODEC.encodeStart(JsonOps.INSTANCE, payload).getOrThrow().getAsJsonObject();

			SkyblockerWebSocket.send(SkyblockerMod.GSON_COMPACT.toJson(encoded));
		} catch (Exception e) {
			LOGGER.info("[Skyblocker WebSocket Message Handler] Failed to send message! Type: {}, Service: {}, Message: {}", type, service, message, e);
		}
	}

	private static Dynamic<?> encodeMessage(Message<? extends Message<?>> message) {
		try {
			@SuppressWarnings("unchecked")
			Codec<Message<?>> codec = (Codec<Message<?>>) message.getCodec();
			Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, codec.encodeStart(JsonOps.INSTANCE, message).getOrThrow());

			return dynamic;
		} catch (Exception e) {
			LOGGER.info("[Skyblocker WebSocket Message Handler] Failed to encode message! Message: {}", message, e);
		}

		return new Dynamic<>(JsonOps.INSTANCE);
	}

	static void handleMessage(String message) {
		try {
			JsonObject payloadEncoded = JsonParser.parseString(message).getAsJsonObject();

			//When status is present its usually a response to a packet being sent or some error, we don't need to pay attention to those
			if (payloadEncoded.has("type")) {
				Payload payload = Payload.CODEC.parse(JsonOps.INSTANCE, payloadEncoded).getOrThrow();

				switch (payload.service()) {
					case Service.CRYSTAL_WAYPOINTS -> CrystalsWaypointMessage.handle(payload.type(), payload.message());
				}
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker WebSocket Message Handler] Failed to handle incoming message!", e);
		}
	}
}
