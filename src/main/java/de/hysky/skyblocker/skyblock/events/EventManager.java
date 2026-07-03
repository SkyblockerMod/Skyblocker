package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.skyblock.events.cyclic.RecurringEvents;

import java.time.Instant;
import java.util.Optional;

public final class EventManager {
	public static Optional<EventInstance> getNext(SkyblockEvent event, Instant after, boolean includeRunning) {
		Optional<EventInstance> fromRepeating = RecurringEvents.getNext(event, after, includeRunning);
		Optional<EventInstance> fromCalendar = CalendarEvents.getNext(event, after, includeRunning);
		return fromCalendar.filter(instance -> fromRepeating.isEmpty() || !instance.start().isAfter(fromRepeating.get().start())).or(() -> fromRepeating);
	}

	public static Optional<EventInstance> getNext(SkyblockEvent event, boolean includeRunning) {
		return getNext(event, Instant.now(), includeRunning);
	}
}
