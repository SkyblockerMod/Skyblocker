package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Matches to a Skyblock item id.
 * 
 * @since 1.22.0
 */
public record ItemIdPredicate(String targetId) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "item_id");
	public static final Codec<ItemIdPredicate> CODEC = Codec.STRING.xmap(ItemIdPredicate::new, ItemIdPredicate::targetId);
	public static final MapCodec<ItemIdPredicate> MAP_CODEC = CODEC.fieldOf("value");

	@Override
	public boolean test(ItemStack stack) {
		return ItemUtils.getItemId(stack).equals(this.targetId);
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.ITEM_ID;
	}
}
