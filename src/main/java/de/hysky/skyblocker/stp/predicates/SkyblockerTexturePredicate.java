package de.hysky.skyblocker.stp.predicates;

import java.util.function.Predicate;

import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import net.minecraft.item.ItemStack;

/**
 * Interface implemented by all Skyblocker Texture Predicates.
 */
public sealed interface SkyblockerTexturePredicate extends Predicate<ItemStack> permits AndPredicate, CoordinateRangePredicate, CustomDataPredicate, ItemIdPredicate, LocationPredicate, NotPredicate, OrPredicate, PetInfoPredicate, RegexPredicate, StringPredicate {
	SkyblockerPredicateType<?> getType();

	/**
	 * Whether an {@link ItemStack} is needed to test this predicate, if this returns false then the {@code stack} passed
	 * to this predicate may be null.
	 */
	default boolean itemStackDependent() {
		return true;
	}
}
