package de.hysky.skyblocker.stp.predicates;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.stp.matchers.RegexMatcher;
import de.hysky.skyblocker.stp.matchers.StringMatcher;
import de.hysky.skyblocker.stp.matchers.TextMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record NamePredicate(Optional<StringMatcher> stringMatcher, Optional<RegexMatcher> regexMatcher, Optional<TextMatcher> textMatcher) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "name");
	public static final Codec<NamePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			StringMatcher.CODEC.optionalFieldOf("stringMatcher").forGetter(NamePredicate::stringMatcher),
			RegexMatcher.CODEC.optionalFieldOf("regexMatcher").forGetter(NamePredicate::regexMatcher),
			TextMatcher.CODEC.optionalFieldOf("textMatcher").forGetter(NamePredicate::textMatcher))
			.apply(instance, NamePredicate::new));

	@Override
	public boolean test(ItemStack stack) {
		Text name = stack.getName();

		if (textMatcher.isPresent() && textMatcher.get().test(name)) return true;

		String stringified = stack.getName().getString();

		if (stringMatcher.isPresent() && stringMatcher.get().test(stringified)) return true;
		if (regexMatcher.isPresent() && regexMatcher.get().test(stringified)) return true;

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.NAME;
	}
}
