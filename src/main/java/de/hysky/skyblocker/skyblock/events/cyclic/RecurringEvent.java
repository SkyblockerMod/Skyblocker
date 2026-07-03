package de.hysky.skyblocker.skyblock.events.cyclic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.EventManager;
import de.hysky.skyblocker.skyblock.events.SkyblockEvents;
import de.hysky.skyblocker.utils.mayor.Election;
import de.hysky.skyblocker.utils.mayor.ElectionCandidate;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.time.SkyblockTimeUnit;
import de.hysky.skyblocker.utils.time.TimeParsing;
import net.minecraft.util.StringRepresentable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @param routineStart When the routine starts
 * @param routine A routine represented by a list of duration, alternates between when the event is active and inactive. Length should be an even number.
 */
// TODO condition (mayor, ...)
public record RecurringEvent(String id, String name, Instant routineStart, List<Duration> routine, EventInstance.AdditionalInfo additionalInfo, Optional<Condition> condition) {
	private static final Duration MAXIMUM_LOOK_AHEAD = Duration.ZERO.plus(12, SkyblockTimeUnit.YEARS);
	public static final Codec<RecurringEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(RecurringEvent::id),
			Codec.STRING.fieldOf("name").forGetter(RecurringEvent::name),
			TimeParsing.INSTANT_CODEC.fieldOf("start").forGetter(RecurringEvent::routineStart),
			TimeParsing.SKY_DURATION_CODEC.listOf(2, Integer.MAX_VALUE)
					.validate(l -> l.size() % 2 == 0 ? DataResult.success(l) : DataResult.error(() -> "Size must be even."))
					.fieldOf("routine").forGetter(RecurringEvent::routine),
			EventInstance.AdditionalInfo.MAP_CODEC.forGetter(RecurringEvent::additionalInfo),
			Condition.CODEC.optionalFieldOf("condition").forGetter(RecurringEvent::condition)
	).apply(instance, RecurringEvent::new));

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
		while (events.size() < amount && after.until(localStart).compareTo(MAXIMUM_LOOK_AHEAD) < 0) {
			while (startIndex % 2 != 0) {
				localStart = localStart.plus(routine.get(startIndex));
				startIndex = (startIndex + 1) % routine.size();
			}
			EventInstance instance = new EventInstance(SkyblockEvents.getOrNew(name), localStart, routine.get(startIndex), Optional.empty(), additionalInfo);
			if (condition.isEmpty() || condition.get().test(instance)) {
				events.add(instance);
			}
			localStart = localStart.plus(routine.get(startIndex));
			startIndex = (startIndex + 1) % routine.size();
		}
		return events;
	}

	public Optional<EventInstance> findNextAfter(Instant after, boolean allowRunning) {
		List<EventInstance> events = findNextAfter(after, allowRunning, 1);
		return events.isEmpty() ? Optional.empty() : Optional.of(events.getFirst());
	}

	public interface Condition extends Predicate<EventInstance> {
		Codec<Condition> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(
				Condition::type,
				Type::codec
		);

		enum Type implements StringRepresentable {
			MAYOR_PERK(MayorPerkCondition.CODEC);

			private final MapCodec<? extends Condition> codec;

			Type(MapCodec<? extends Condition> codec) {
				this.codec = codec;
			}

			public MapCodec<? extends Condition> codec() {
				return codec;
			}

			@Override
			public String getSerializedName() {
				return name().toLowerCase(Locale.ENGLISH);
			}
		}

		Type type();
	}

	public record MayorPerkCondition(String perk, Optional<String> description) implements Condition {
		public static final MapCodec<MayorPerkCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.fieldOf("perk").forGetter(MayorPerkCondition::perk),
				Codec.STRING.optionalFieldOf("description").forGetter(MayorPerkCondition::description)
		).apply(instance, MayorPerkCondition::new));

		@Override
		public Type type() {
			return Type.MAYOR_PERK;
		}

		@Override
		public boolean test(EventInstance instance) {
			Optional<EventInstance> electionEvent = EventManager.getNext(SkyblockEvents.MAYOR_ELECTION, true);
			// Current mayor term ends before the event, gotta check the election
			if (electionEvent.isPresent() && !electionEvent.get().end().isAfter(instance.start())) {
				// give up if the event is after the next mayor term
				if (EventManager.getNext(SkyblockEvents.MAYOR_ELECTION, electionEvent.get().end(), false)
						.map(EventInstance::end)
						.filter(end -> end.isBefore(instance.start())).isPresent()) return false;
				if (MayorUtils.election().isEmpty()) return false;
				Election election = MayorUtils.election().get();
				return checkCandidate(election.mostVotes(), false) || checkCandidate(election.secondMostVotes(), true);
			} else {
				return MayorUtils.getActivePerks().hasPerk(perk, description.orElse(null));
			}
		}

		private boolean checkCandidate(ElectionCandidate candidate, boolean minister) {
			return candidate.hasPerk(perk, description.orElse(null), minister);
		}
	}
}
