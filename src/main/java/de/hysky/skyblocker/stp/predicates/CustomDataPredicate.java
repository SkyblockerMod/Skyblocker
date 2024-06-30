package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.util.Identifier;

/**
 * Allows for matching on arbitrary custom data. Intended for use when there is no specific predicate for your use case (yet).
 * 
 * @since 1.22.0
 * 
 * @see {@link net.minecraft.predicate.item.CustomDataPredicate}
 */
public record CustomDataPredicate(NbtPredicate predicate) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "custom_data");
	public static final Codec<CustomDataPredicate> CODEC = NbtPredicate.CODEC.xmap(CustomDataPredicate::new, CustomDataPredicate::predicate);
	public static final MapCodec<CustomDataPredicate> MAP_CODEC = CODEC.fieldOf("value");

	@Override
	public boolean test(ItemStack stack) {
		return this.predicate.test(stack);
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.CUSTOM_DATA;
	}
}
