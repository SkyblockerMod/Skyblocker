package de.hysky.skyblocker.stp.matchers;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

public record RegexMatcher(Pattern regex, MatchType matchType) implements Predicate<String> {
	public static final Codec<RegexMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codecs.REGULAR_EXPRESSION.fieldOf("regex").forGetter(RegexMatcher::regex),
			MatchType.CODEC.optionalFieldOf("matchType", MatchType.FULL).forGetter(RegexMatcher::matchType))
			.apply(instance, RegexMatcher::new));

	@Override
	public boolean test(String string) {
		return switch (matchType) {
			case MatchType.FULL -> regex.matcher(string).matches();
			case MatchType.PARTIAL -> regex.matcher(string).find();

			default -> false;
		};
	}

	private enum MatchType implements StringIdentifiable {
		FULL,
		PARTIAL;

		private static final Codec<MatchType> CODEC = StringIdentifiable.createBasicCodec(MatchType::values);

		@Override
		public String asString() {
			return name();
		}
	}
}
