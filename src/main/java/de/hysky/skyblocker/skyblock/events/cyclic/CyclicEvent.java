package de.hysky.skyblocker.skyblock.events.cyclic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.SkyblockEvents;
import de.hysky.skyblocker.utils.time.TimeParsing;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @param routineStart When the routine starts
 * @param routine A routine represented by a list of duration, alternates between when the event is active and inactive. Length should be an even number.
 */
// TODO condition (mayor, ...)
public record CyclicEvent(String id, String name, Instant routineStart, List<Duration> routine, EventInstance.AdditionalInfo additionalInfo) {
	public static final Codec<CyclicEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(CyclicEvent::id),
			Codec.STRING.fieldOf("name").forGetter(CyclicEvent::name),
			TimeParsing.INSTANT_CODEC.fieldOf("start").forGetter(CyclicEvent::routineStart),
			TimeParsing.SKY_DURATION_CODEC.listOf(2, Integer.MAX_VALUE)
					.validate(l -> l.size() % 2 == 0 ? DataResult.success(l) : DataResult.error(() -> "Size must be even."))
					.fieldOf("routine").forGetter(CyclicEvent::routine),
			EventInstance.AdditionalInfo.MAP_CODEC.forGetter(CyclicEvent::additionalInfo)
	).apply(instance, CyclicEvent::new));

	public List<EventInstance> findNextAfter(Instant after, boolean allowRunning, int amount) {
		if (amount <= 0) return List.of();
		Duration totalRoutine = Duration.ofMillis(routine.stream().mapToLong(Duration::toMillis).sum());
		Instant localStart = routineStart.plus(totalRoutine.multipliedBy(routineStart.until(after).dividedBy(totalRoutine)));
		List<EventInstance> events = new ArrayList<>(amount);
		int startIndex = 0;
		for (int i = 0; i < routine.size(); i++) {
			Duration duration = routine.get(i);
			Instant end = localStart.plus(duration);
			if (end.isAfter(after)) {
				if (i % 2 == 0 && allowRunning) {
					startIndex = i;
					break;
				}
				startIndex = (i + 1) % routine.size();
				localStart = localStart.plus(duration);
				break;
			}
			localStart = localStart.plus(duration);
		}
		while (events.size() < amount) {
			while (startIndex % 2 != 0) {
				localStart = localStart.plus(routine.get(startIndex));
				startIndex = (startIndex + 1) % routine.size();
			}
			events.add(new EventInstance(SkyblockEvents.getOrNew(name), localStart, routine.get(startIndex), Optional.empty(), additionalInfo));
			localStart = localStart.plus(routine.get(startIndex));
			startIndex = (startIndex + 1) % routine.size();
		}
		return events;
	}

	public Optional<EventInstance> findNextAfter(Instant after, boolean allowRunning) {
		List<EventInstance> events = findNextAfter(after, allowRunning, 1);
		return events.isEmpty() ? Optional.empty() : Optional.of(events.getFirst());
	}
}
