package de.hysky.skyblocker.stp.matchers;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StringMatcher(Optional<String> equals, Optional<String> startsWith, Optional<String> endsWith, Optional<String> contains) implements Predicate<String> {
	public static final Codec<StringMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("equals").forGetter(StringMatcher::equals),
			Codec.STRING.optionalFieldOf("startsWith").forGetter(StringMatcher::startsWith),
			Codec.STRING.optionalFieldOf("endsWith").forGetter(StringMatcher::endsWith),
			Codec.STRING.optionalFieldOf("contains").forGetter(StringMatcher::contains))
			.apply(instance, StringMatcher::new));

	@Override
	public boolean test(String string) {
		if (equals.isPresent() && !string.equals(equals.get())) return false;
		if (startsWith.isPresent() && !string.startsWith(startsWith.get())) return false;
		if (endsWith.isPresent() && !string.endsWith(endsWith.get())) return false;
		if (contains.isPresent() && !string.contains(contains.get())) return false;

		return true;
	}
}
