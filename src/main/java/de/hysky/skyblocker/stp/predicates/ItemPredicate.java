package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record ItemPredicate(Item item) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "item");
	public static final Codec<ItemPredicate> CODEC = Identifier.CODEC
			.xmap(Registries.ITEM::get, Registries.ITEM::getId)
			.xmap(ItemPredicate::new, ItemPredicate::item);

	@Override
	public boolean test(ItemStack stack) {
		return stack.isOf(item);
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.ITEM;
	}
}
