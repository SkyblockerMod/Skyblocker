package de.hysky.skyblocker.stp.predicates;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Performs a {@code Logical OR} on the {@code predicates}. In layman's terms, when this predicate is tested it will return {@code true} if
 * any of the {@code predicates} also return {@code true}.
 * 
 * @since 1.22.0
 */
public record OrPredicate(List<SkyblockerTexturePredicate> predicates) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "or");
	private static final Codec<SkyblockerTexturePredicate> PREDICATE_CODEC = Codec.lazyInitialized(() -> SkyblockerPredicateType.REGISTRY.getCodec()
			.dispatch("id", SkyblockerTexturePredicate::getType, SkyblockerPredicateType::mapCodec));
	public static final Codec<OrPredicate> CODEC = Codec.lazyInitialized(() -> PREDICATE_CODEC.listOf().xmap(OrPredicate::new, OrPredicate::predicates));
	public static final MapCodec<OrPredicate> MAP_CODEC = CODEC.fieldOf("value");

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
