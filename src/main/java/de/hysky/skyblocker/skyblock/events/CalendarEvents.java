package de.hysky.skyblocker.skyblock.events;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Http;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Handles events that are parsed from the in-game calendar
 */
public final class CalendarEvents {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static Map<SkyblockEvent, LinkedList<EventInstance>> events = new Reference2ObjectOpenHashMap<>();

	@Init
	public static void init() {
		de.hysky.skyblocker.events.SkyblockEvents.JOIN.register(CalendarEvents::refreshEvents);
	}

	public static void refreshEvents() {
		CompletableFuture.supplyAsync(() -> {
			try {
				return Http.sendGetRequest("https://hysky.de/api/calendar");
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to download events list", e);
			}
			return null;
		}, Executors.newVirtualThreadPerTaskExecutor()).thenAccept(response -> {
			Map<SkyblockEvent, LinkedList<EventInstance>> newEvents = new Reference2ObjectOpenHashMap<>();
			if (response == null) {
				LOGGER.error("[Skyblocker] Failed to get events list");
				return;
			}

			List<EventInstance> parsedEvents = EventInstance.CODEC.listOf().parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getPartialOrThrow();
			for (EventInstance event : parsedEvents) {
				newEvents.computeIfAbsent(event.event(), _ -> new LinkedList<>()).add(event);
			}

			for (Map.Entry<SkyblockEvent, LinkedList<EventInstance>> entry : newEvents.entrySet()) {
				entry.getValue().sort(Comparator.comparing(EventInstance::start)); // Sort just in case it's not in order for some reason in API
				//LOGGER.info("Next {} is at {}", entry.getKey(), entry.getValue().peekFirst());
			}

			SkyblockerConfigManager.update(config -> {
				for (SkyblockEvent s : newEvents.keySet()) {
					config.eventNotifications.events.computeIfAbsent(s.name(), _ -> EventNotifications.DEFAULT_REMINDERS);
				}
			});
			events = newEvents;
		}).exceptionally(CalendarEvents::itBorked);
	}

	private static Void itBorked(Throwable throwable) {
		LOGGER.error("[Skyblocker] Event loading borked, sowwy :(", throwable);
		return null;
	}

	public static Optional<EventInstance> getNext(SkyblockEvent event, Instant after, boolean includeRunning) {
		if (!events.containsKey(event)) return Optional.empty();
		return events.get(event).stream()
				.filter(instance -> includeRunning ? instance.end().isAfter(after) : instance.start().isAfter(after))
				.findFirst();
	}
}
