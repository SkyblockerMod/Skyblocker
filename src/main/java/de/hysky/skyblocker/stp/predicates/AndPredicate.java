package de.hysky.skyblocker.stp.predicates;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.stp.SkyblockerTexturePredicates;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Performs a {@code Logical AND} on all {@code predicates}. In layman's terms, when this predicate is tested it will return {@code true} if
 * all of the {@code predicates} also return {@code true}.
 */
public record AndPredicate(List<Map<String, SkyblockerTexturePredicate>> predicateMaps, SkyblockerTexturePredicate[] predicates) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "and");
	public static final Codec<AndPredicate> CODEC = Codec.lazyInitialized(() -> SkyblockerTexturePredicates.MAP_LIST_CODEC.xmap(AndPredicate::of, AndPredicate::predicateMaps));

	private static AndPredicate of(List<Map<String, SkyblockerTexturePredicate>> predicateMaps) {
		return new AndPredicate(predicateMaps, SkyblockerTexturePredicates.flattenMap(predicateMaps));
	}

	@Override
	public boolean test(ItemStack stack) {
		for (SkyblockerTexturePredicate predicate : this.predicates) {
			if (!predicate.test(stack)) return false;
		}

		return true;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.AND;
	}
}
