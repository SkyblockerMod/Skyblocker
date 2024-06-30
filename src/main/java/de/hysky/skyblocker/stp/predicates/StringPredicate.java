package de.hysky.skyblocker.stp.predicates;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

/**
 * Allows for matching via a string on an item's name or lore; supports full matches with {@link String#equals},
 * and partial matches via {@link String#contains}, {@link String#startsWith}, and {@link String#endsWith}.
 * 
 * @since 1.22.0
 */
public record StringPredicate(MatchType matchType, MatchTarget target, String string) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "string");
	public static final MapCodec<StringPredicate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			MatchType.CODEC.fieldOf("matchType").forGetter(StringPredicate::matchType),
			MatchTarget.CODEC.fieldOf("target").forGetter(StringPredicate::target),
			Codec.STRING.fieldOf("string").forGetter(StringPredicate::string))
			.apply(instance, StringPredicate::new));
	public static final Codec<StringPredicate> CODEC = MAP_CODEC.codec();

	@Override
	public boolean test(ItemStack stack) {
		switch (this.target) {
			case LORE -> {
				if (stack.contains(DataComponentTypes.LORE)) {
					List<Text> lore = stack.get(DataComponentTypes.LORE).lines();

					for (Text line : lore) {
						switch (this.matchType) {
							case EQUALS -> {
								if (line.getString().equals(this.string)) return true;
							}

							case CONTAINS -> {
								if (line.getString().contains(this.string)) return true;
							}

							case STARTS_WITH -> {
								if (line.getString().startsWith(this.string)) return true;
							}

							case ENDS_WITH -> {
								if (line.getString().endsWith(this.string)) return true;
							}
						}
					}
				}
			}

			case NAME -> {
				return switch (this.matchType) {
					case EQUALS -> stack.getName().getString().equals(this.string);
					case CONTAINS -> stack.getName().getString().contains(this.string);
					case STARTS_WITH -> stack.getName().getString().startsWith(this.string);
					case ENDS_WITH -> stack.getName().getString().endsWith(this.string);
				};
			}
		}

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.STRING;
	}

	private enum MatchType implements StringIdentifiable {
		EQUALS,
		CONTAINS,
		STARTS_WITH,
		ENDS_WITH;

		private static final Codec<MatchType> CODEC = StringIdentifiable.createCodec(MatchType::values);

		@Override
		public String asString() {
			return name();
		}
	}
}
