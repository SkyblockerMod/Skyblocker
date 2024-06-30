package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Matches to a Skyblock {@link Location} id.
 */
public record LocationPredicate(Location location) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "location");
	public static final Codec<LocationPredicate> CODEC = Location.CODEC.xmap(LocationPredicate::new, LocationPredicate::location);

	@Override
	public boolean test(ItemStack stack) {
		return Utils.getLocation() == this.location;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.LOCATION;
	}
}
