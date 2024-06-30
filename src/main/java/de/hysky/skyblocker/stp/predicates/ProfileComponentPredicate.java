package de.hysky.skyblocker.stp.predicates;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.stp.matchers.RegexMatcher;
import de.hysky.skyblocker.stp.matchers.StringMatcher;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

/**
 * Allows for matching to the {@code id} (UUID) field of a profile component, and for matching the texture base64.
 */
public record ProfileComponentPredicate(Optional<UUID> uuid, Optional<TextureMatcher> textureMatcher) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "profile_component");
	public static final Codec<ProfileComponentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Uuids.STRING_CODEC.optionalFieldOf("uuid").forGetter(ProfileComponentPredicate::uuid),
			TextureMatcher.CODEC.optionalFieldOf("textureMatcher").forGetter(ProfileComponentPredicate::textureMatcher))
			.apply(instance, ProfileComponentPredicate::new));

	@Override
	public boolean test(ItemStack stack) {
		if (stack.contains(DataComponentTypes.PROFILE)) {
			ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);

			if (uuid.isPresent() && profile.id().isPresent() && !profile.id().get().equals(uuid.get())) return false;
			if (textureMatcher.isPresent() && !textureMatcher.get().test(stack)) return false;

			return true;
		}

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.PROFILE_COMPONENT;
	}

	//We could add a target in the future if we want to allow matching on more than the base64 (e.g. the skin hash)
	private record TextureMatcher(Optional<StringMatcher> stringMatcher, Optional<RegexMatcher> regexMatcher) implements Predicate<ItemStack> {
		private static final Codec<TextureMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				StringMatcher.CODEC.optionalFieldOf("stringMatcher").forGetter(TextureMatcher::stringMatcher),
				RegexMatcher.CODEC.optionalFieldOf("regexMatcher").forGetter(TextureMatcher::regexMatcher))
				.apply(instance, TextureMatcher::new));

		@Override
		public boolean test(ItemStack stack) {
			if (stack.isOf(Items.PLAYER_HEAD)) {
				String textureBase64 = ItemUtils.getHeadTexture(stack);

				if (stringMatcher.isPresent() && stringMatcher.get().test(textureBase64)) return true;
				if (regexMatcher.isPresent() && regexMatcher.get().test(textureBase64)) return true;
			}

			return false;
		}
	}
}
