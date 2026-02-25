package de.hysky.skyblocker.utils.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.ApiAuthentication;
import de.hysky.skyblocker.utils.Http;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class SkyblockerWebSocket {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String WS_URL = "wss://ws.hysky.de";
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(Redirect.NORMAL)
			.executor(Executors.newVirtualThreadPerTaskExecutor())
			.version(Version.HTTP_2)
			.build();
	private static final ExecutorService MESSAGE_SEND_QUEUE = Executors.newSingleThreadExecutor(Thread.ofVirtual()
			.name("Skyblocker WebSocket Message Send Queue")
			.factory());
	private static volatile @Nullable WebSocket socket;

	@Init
	public static void init() {
		SkyblockEvents.JOIN.register(() -> {
			if (!isConnectionOpen()) setupSocket();
		});
		// Make a best effort to send a message to the WS indicating the game & connection will close.
		// The delivery of this message is not guaranteed since this will not block the game from closing.
		ClientLifecycleEvents.CLIENT_STOPPING.register(_minecraft -> closeSocket());
	}

	private static CompletableFuture<Void> setupSocket() {
		return CompletableFuture.runAsync(() -> {
			try {
				socket = HTTP_CLIENT.newWebSocketBuilder()
						.header("Authorization", "Bearer " + Objects.requireNonNull(ApiAuthentication.getToken(), "Token cannot be null"))
						.header("User-Agent", Http.USER_AGENT)
						.buildAsync(URI.create(WS_URL), new SocketListener())
						.get();

				LOGGER.info("[Skyblocker WebSocket] Successfully connected to the Skyblocker WebSocket!");
			} catch (Exception e) {
				if (e instanceof WebSocketHandshakeException wsHandshakeException) {
					LOGGER.error("[Skyblocker WebSocket] Failed to setup WebSocket connection! Http Response: {}", wsHandshakeException.getResponse(), e);
				} else {
					LOGGER.error("[Skyblocker WebSocket] Failed to setup WebSocket connection!", e);
				}
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static void closeSocket() {
		if (isConnectionOpen()) {
			MESSAGE_SEND_QUEUE.submit(() -> {
				socket.sendClose(WebSocket.NORMAL_CLOSURE, "Minecraft closing");
			});
		}
	}

	private static boolean isConnectionOpen() {
		return socket != null && !socket.isInputClosed() && !socket.isOutputClosed();
	}

	static void send(String message) {
		if (isConnectionOpen()) {
			sendInternal(message);
		} else {
			setupSocket().thenRun(() -> sendInternal(message));
		}
	}

	private static void sendInternal(String message) {
		MESSAGE_SEND_QUEUE.submit(() -> {
			try {
				if (Debug.debugEnabled() && Debug.webSocketDebug()) {
					LOGGER.info("[Skyblocker WebSocket] Sending Message: {}", message);
				}

				socket.sendText(message, true).join();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker WebSocket] Failed to send message!", e);
			}
		});
	}

	private static class SocketListener implements WebSocket.Listener {
		private List<CharSequence> parts = new ArrayList<>();
		private CompletableFuture<?> accumulatedMessage = new CompletableFuture<>();

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
			this.parts.add(message);
			webSocket.request(1);

			if (last) {
				//Process message once we've got all the text
				handleWholeMessage(this.parts);

				//Reset state and allow CharSequences to be reclaimed or something? Java WebSockets are very confusing
				this.parts = new ArrayList<>();
				this.accumulatedMessage.complete(null);
				CompletionStage<?> future = this.accumulatedMessage;
				this.accumulatedMessage = new CompletableFuture<>();

				return future;
			}

			return this.accumulatedMessage;
		}

		@Override
		public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
			if (Debug.debugEnabled() && Debug.webSocketDebug()) {
				LOGGER.info("[Skyblocker WebSocket] Received ping");
			}

			return WebSocket.Listener.super.onPing(webSocket, message);
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			LOGGER.info("[Skyblocker WebSocket] Connection closing. Status Code: {}, Reason: {}", statusCode, reason);
			socket = null;

			return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			LOGGER.error("[Skyblocker WebSocket] Encountered an error and closed the connection!", error);
		}

		private void handleWholeMessage(List<CharSequence> parts) {
			String message = String.join("", parts);

			if (Debug.debugEnabled() && Debug.webSocketDebug()) {
				LOGGER.info("[Skyblocker WebSocket] Received Message: {}", message);
			}

			WsMessageHandler.handleMessage(message);
		}
	}
}
