package de.hysky.skyblocker.stp.matchers;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.LongPredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.dynamic.Codecs;

public record LongMatcher(OptionalLong value, NumericOperator operator, Optional<LongRange> range) implements LongPredicate {
	public static final Codec<LongMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codecs.optionalLong(Codec.LONG.optionalFieldOf("value")).forGetter(LongMatcher::value),
			NumericOperator.CODEC.optionalFieldOf("operator", NumericOperator.EQUAL).forGetter(LongMatcher::operator),
			LongRange.CODEC.optionalFieldOf("range").forGetter(LongMatcher::range))
			.apply(instance, LongMatcher::new));

	@Override
	public boolean test(long input) {
		if (range.isPresent()) return range.get().test(input);

		if (value.isPresent()) {
			return switch (operator) {
				case EQUAL -> input == value.getAsLong();
				case NOT_EQUAL -> input != value.getAsLong();
				case LESS_THAN -> input < value.getAsLong();
				case LESS_THAN_OR_EQUAL_TO -> input <= value.getAsLong();
				case GREATER_THAN -> input > value.getAsLong();
				case GREATER_THAN_OR_EQUAL_TO -> input >= value.getAsLong();
			};
		}

		return false;
	}

	public record LongRange(long min, long max, boolean inclusive) implements LongPredicate {
		public static final Codec<LongRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("min").forGetter(LongRange::min),
				Codec.LONG.fieldOf("max").forGetter(LongRange::max),
				Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(LongRange::inclusive))
				.apply(instance, LongRange::new));

		@Override
		public boolean test(long value) {
			return inclusive ? value >= min && value <= max : value > min && value < max;
		}
	}
}
