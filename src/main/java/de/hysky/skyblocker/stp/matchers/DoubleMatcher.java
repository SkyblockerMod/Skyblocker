package de.hysky.skyblocker.stp.matchers;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.DoublePredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.utils.CodecUtils;

public record DoubleMatcher(OptionalDouble value, NumericOperator operator, Optional<DoubleRange> range) implements DoublePredicate {
	public static final Codec<DoubleMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecUtils.optionalDouble(Codec.DOUBLE.optionalFieldOf("value")).forGetter(DoubleMatcher::value),
			NumericOperator.CODEC.optionalFieldOf("operator", NumericOperator.EQUAL).forGetter(DoubleMatcher::operator),
			DoubleRange.CODEC.optionalFieldOf("range").forGetter(DoubleMatcher::range))
			.apply(instance, DoubleMatcher::new));

	@Override
	public boolean test(double input) {
		if (range.isPresent()) return range.get().test(input);

		if (value.isPresent()) {
			return switch (operator) {
				case EQUAL -> input == value.getAsDouble();
				case NOT_EQUAL -> input != value.getAsDouble();
				case LESS_THAN -> input < value.getAsDouble();
				case LESS_THAN_OR_EQUAL_TO -> input <= value.getAsDouble();
				case GREATER_THAN -> input > value.getAsDouble();
				case GREATER_THAN_OR_EQUAL_TO -> input >= value.getAsDouble();
			};
		}

		return false;
	}

	public record DoubleRange(double min, double max, boolean inclusive) implements DoublePredicate {
		public static final Codec<DoubleRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.fieldOf("min").forGetter(DoubleRange::min),
				Codec.DOUBLE.fieldOf("max").forGetter(DoubleRange::max),
				Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(DoubleRange::inclusive))
				.apply(instance, DoubleRange::new));

		@Override
		public boolean test(double value) {
			return inclusive ? value >= min && value <= max : value > min && value < max;
		}
	}
}
