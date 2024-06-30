package de.hysky.skyblocker.stp.predicates;

import java.util.function.Predicate;

import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import net.minecraft.item.ItemStack;

/**
 * Interface implemented by all Skyblocker Texture Predicates.
 */
public sealed interface SkyblockerTexturePredicate extends Predicate<ItemStack> permits AndPredicate, ApiIdPredicate, CustomDataPredicate, DyedPredicate, HeldByArmorStandPredicate, InsideScreenPredicate, ItemPredicate, LocationPredicate, LorePredicate, NamePredicate, NotPredicate, OrPredicate, PetInfoPredicate, ProfileComponentPredicate {

	SkyblockerPredicateType<?> getType();
}
