package de.hysky.skyblocker.skyblock.events.cyclic;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @param routineStart When the routine starts
 * @param routine A routine represented by a list of duration, alternates between when the event is active and inactive. Length should be an even number.
 */
// TODO condition (mayor, ...)
public record CyclicEvent(String id, String name, Instant routineStart, List<Duration> routine) {
}
