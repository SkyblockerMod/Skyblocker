package de.hysky.skyblocker.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ChatEvents {
	/**
	 * This will be called when a game message is received, cancelled or not.
	 */
	public static final Event<ChatTextEvent> RECEIVE_TEXT = EventFactory.createArrayBacked(ChatTextEvent.class, listeners -> message -> {
		for (ChatTextEvent listener : listeners) {
			listener.onMessage(message);
		}
	});

	/**
	 * This will be called when a game message is received, cancelled or not.
	 * This method is called with the result of {@link Text#getString()} to avoid each listener having to call it.
	 */
	public static final Event<ChatStringEvent> RECEIVE_STRING = EventFactory.createArrayBacked(ChatStringEvent.class, listeners -> message -> {
		for (ChatStringEvent listener : listeners) {
			listener.onMessage(message);
		}
	});

	@FunctionalInterface
	public interface ChatTextEvent {
		void onMessage(Text message);
	}

	@FunctionalInterface
	public interface ChatStringEvent {
		void onMessage(String message);
	}
}
