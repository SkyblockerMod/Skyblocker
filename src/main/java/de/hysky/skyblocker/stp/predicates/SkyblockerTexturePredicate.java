package de.hysky.skyblocker.stp.predicates;

import java.util.function.Predicate;

import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import net.minecraft.item.ItemStack;

/**
 * Interface implemented by all Skyblocker Texture Predicates.
 */
public sealed interface SkyblockerTexturePredicate extends Predicate<ItemStack> permits AndPredicate, CustomDataPredicate, ItemIdPredicate, LocationPredicate, OrPredicate, PetInfoPredicate, RegexPredicate, StringPredicate {
	SkyblockerPredicateType<?> getType();
}
