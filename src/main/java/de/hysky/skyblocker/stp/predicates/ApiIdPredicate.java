package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Allows for matching to the API ID of an item.
 */
public record ApiIdPredicate(String targetId) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "api_id");
	public static final Codec<ApiIdPredicate> CODEC = Codec.STRING.xmap(ApiIdPredicate::new, ApiIdPredicate::targetId);

	@Override
	public boolean test(ItemStack stack) {
		return stack.getSkyblockApiId().equals(targetId);
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.API_ID;
	}
}
