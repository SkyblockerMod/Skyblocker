package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Allows for checking whether the {@link ItemStack} is held by an armor stand or not.
 */
public record HeldByArmorStandPredicate(boolean value) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "held_by_armor_stand");
	public static final Codec<HeldByArmorStandPredicate> CODEC = Codec.BOOL.xmap(HeldByArmorStandPredicate::new, HeldByArmorStandPredicate::value);

	@Override
	public boolean test(ItemStack stack) {
		return stack.getHolder() instanceof ArmorStandEntity == value;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.HELD_BY_ARMOR_STAND;
	}
}
