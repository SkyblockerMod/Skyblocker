package de.hysky.skyblocker.stp.matchers;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record TextMatcher(Optional<Text> text, Optional<StringMatcher> stringMatcher, Optional<RegexMatcher> regexMatcher) implements Predicate<Text> {
	public static final Codec<TextMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TextCodecs.STRINGIFIED_CODEC.optionalFieldOf("text").forGetter(TextMatcher::text),
			StringMatcher.CODEC.optionalFieldOf("stringMatcher").forGetter(TextMatcher::stringMatcher),
			RegexMatcher.CODEC.optionalFieldOf("regexMatcher").forGetter(TextMatcher::regexMatcher))
			.apply(instance, TextMatcher::new));

	@Override
	public boolean test(Text text) {
		if (this.text.isPresent()) return this.text.get().equals(text);

		if (stringMatcher.isPresent() || regexMatcher.isPresent()) {
			try {
				String stringified = TextCodecs.STRINGIFIED_CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow().toString();

				if (stringMatcher.isPresent() && stringMatcher.get().test(stringified)) return true;
				if (regexMatcher.isPresent() && regexMatcher.get().test(stringified)) return true;
			} catch (Exception ignored) {}
		}

		return false;
	}
}
