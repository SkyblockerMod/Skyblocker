package de.hysky.skyblocker.stp.matchers;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntPredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.utils.CodecUtils;

public record IntMatcher(OptionalInt value, NumericOperator operator, Optional<IntRange> range) implements IntPredicate {
	public static final Codec<IntMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecUtils.optionalInt(Codec.INT.optionalFieldOf("value")).forGetter(IntMatcher::value),
			NumericOperator.CODEC.optionalFieldOf("operator", NumericOperator.EQUAL).forGetter(IntMatcher::operator),
			IntRange.CODEC.optionalFieldOf("range").forGetter(IntMatcher::range))
			.apply(instance, IntMatcher::new));

	@Override
	public boolean test(int input) {
		if (range.isPresent()) return range.get().test(input);

		if (value.isPresent()) {
			return switch (operator) {
				case EQUAL -> input == value.getAsInt();
				case NOT_EQUAL -> input != value.getAsInt();
				case LESS_THAN -> input < value.getAsInt();
				case LESS_THAN_OR_EQUAL_TO -> input <= value.getAsInt();
				case GREATER_THAN -> input > value.getAsInt();
				case GREATER_THAN_OR_EQUAL_TO -> input >= value.getAsInt();
			};
		}

		return false;
	}

	public record IntRange(int min, int max, boolean inclusive) implements IntPredicate {
		public static final Codec<IntRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("min").forGetter(IntRange::min),
				Codec.INT.fieldOf("max").forGetter(IntRange::max),
				Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(IntRange::inclusive))
				.apply(instance, IntRange::new));

		@Override
		public boolean test(int value) {
			return inclusive ? value >= min && value <= max : value > min && value < max;
		}
	}
}
