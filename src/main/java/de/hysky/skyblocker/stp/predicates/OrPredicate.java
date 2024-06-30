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
 * Performs a {@code Logical OR} on the {@code predicates}. In layman's terms, when this predicate is tested it will return {@code true} if
 * any of the {@code predicates} also return {@code true}.
 */
public record OrPredicate(List<Map<String, SkyblockerTexturePredicate>> predicateMaps, SkyblockerTexturePredicate[] predicates) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "or");
	public static final Codec<OrPredicate> CODEC = Codec.lazyInitialized(() -> SkyblockerTexturePredicates.MAP_LIST_CODEC.xmap(OrPredicate::of, OrPredicate::predicateMaps));

	private static OrPredicate of(List<Map<String, SkyblockerTexturePredicate>> predicateMaps) {
		return new OrPredicate(predicateMaps, SkyblockerTexturePredicates.flattenMap(predicateMaps));
	}

	@Override
	public boolean test(ItemStack stack) {
		for (SkyblockerTexturePredicate predicate : this.predicates) {
			if (predicate.test(stack)) return true;
		}

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.OR;
	}
}
