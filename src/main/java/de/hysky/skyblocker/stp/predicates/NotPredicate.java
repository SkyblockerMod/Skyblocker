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
 * Performs a {@code Logical NOT} on the {@code predicates}. In layman's terms, when this predicate is tested it will return {@code true}
 * only if all {@code predicates} return {@code false}.
 */
public record NotPredicate(List<Map<String, SkyblockerTexturePredicate>> predicateMaps, SkyblockerTexturePredicate[] predicates) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "not");
	public static final Codec<NotPredicate> CODEC = Codec.lazyInitialized(() -> SkyblockerTexturePredicates.MAP_LIST_CODEC.xmap(NotPredicate::of, NotPredicate::predicateMaps));

	private static NotPredicate of(List<Map<String, SkyblockerTexturePredicate>> predicateMaps) {
		return new NotPredicate(predicateMaps, SkyblockerTexturePredicates.flattenMap(predicateMaps));
	}

	@Override
	public boolean test(ItemStack stack) {
		for (SkyblockerTexturePredicate predicate : this.predicates) {
			if (predicate.test(stack)) return false;
		}

		return true;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.NOT;
	}
}
