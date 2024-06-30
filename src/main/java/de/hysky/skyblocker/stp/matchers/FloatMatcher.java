package de.hysky.skyblocker.stp.matchers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.floats.FloatPredicate;

public record FloatMatcher(Optional<Float> value, NumericOperator operator, Optional<FloatRange> range) implements FloatPredicate {
	public static final Codec<FloatMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("value").forGetter(FloatMatcher::value),
			NumericOperator.CODEC.optionalFieldOf("operator", NumericOperator.EQUAL).forGetter(FloatMatcher::operator),
			FloatRange.CODEC.optionalFieldOf("range").forGetter(FloatMatcher::range))
			.apply(instance, FloatMatcher::new));

	@Override
	public boolean test(float input) {
		if (range.isPresent()) return range.get().test(input);

		if (value.isPresent()) {
			return switch (operator) {
				case EQUAL -> input == value.get();
				case NOT_EQUAL -> input != value.get();
				case LESS_THAN -> input < value.get();
				case LESS_THAN_OR_EQUAL_TO -> input <= value.get();
				case GREATER_THAN -> input > value.get();
				case GREATER_THAN_OR_EQUAL_TO -> input >= value.get();
			};
		}

		return false;
	}

	public record FloatRange(float min, float max, boolean inclusive) implements FloatPredicate {
		public static final Codec<FloatRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("min").forGetter(FloatRange::min),
				Codec.FLOAT.fieldOf("max").forGetter(FloatRange::max),
				Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(FloatRange::inclusive))
				.apply(instance, FloatRange::new));

		@Override
		public boolean test(float value) {
			return inclusive ? value >= min && value <= max : value > min && value < max;
		}
	}
}
