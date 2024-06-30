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
 * Performs a {@code Logical NOT} on the {@code predicates}. In layman's terms, when this predicate is tested it will return {@code true}
 * only if all {@code predicates} return {@code false}.
 * 
 * @since 1.22.0
 */
public record NotPredicate(List<SkyblockerTexturePredicate> predicates) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "not");
	private static final Codec<SkyblockerTexturePredicate> PREDICATE_CODEC = Codec.lazyInitialized(() -> SkyblockerPredicateType.REGISTRY.getCodec()
			.dispatch("id", SkyblockerTexturePredicate::getType, SkyblockerPredicateType::mapCodec));
	public static final Codec<NotPredicate> CODEC = Codec.lazyInitialized(() -> PREDICATE_CODEC.listOf().xmap(NotPredicate::new, NotPredicate::predicates));
	public static final MapCodec<NotPredicate> MAP_CODEC = CODEC.fieldOf("value");

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
