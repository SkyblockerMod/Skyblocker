package de.hysky.skyblocker.stp;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.stp.predicates.AndPredicate;
import de.hysky.skyblocker.stp.predicates.ApiIdPredicate;
import de.hysky.skyblocker.stp.predicates.CustomDataPredicate;
import de.hysky.skyblocker.stp.predicates.DyedPredicate;
import de.hysky.skyblocker.stp.predicates.HeldByArmorStandPredicate;
import de.hysky.skyblocker.stp.predicates.InsideScreenPredicate;
import de.hysky.skyblocker.stp.predicates.ItemPredicate;
import de.hysky.skyblocker.stp.predicates.LocationPredicate;
import de.hysky.skyblocker.stp.predicates.LorePredicate;
import de.hysky.skyblocker.stp.predicates.NamePredicate;
import de.hysky.skyblocker.stp.predicates.NotPredicate;
import de.hysky.skyblocker.stp.predicates.OrPredicate;
import de.hysky.skyblocker.stp.predicates.PetInfoPredicate;
import de.hysky.skyblocker.stp.predicates.ProfileComponentPredicate;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface SkyblockerPredicateTypes {
	SkyblockerPredicateType<AndPredicate> AND = register(AndPredicate.ID, new SkyblockerPredicateType<>(AndPredicate.CODEC));
	SkyblockerPredicateType<ApiIdPredicate> API_ID = register(ApiIdPredicate.ID, new SkyblockerPredicateType<>(ApiIdPredicate.CODEC));
	SkyblockerPredicateType<CustomDataPredicate> CUSTOM_DATA = register(CustomDataPredicate.ID, new SkyblockerPredicateType<>(CustomDataPredicate.CODEC));
	SkyblockerPredicateType<DyedPredicate> DYED = register(DyedPredicate.ID, new SkyblockerPredicateType<>(DyedPredicate.CODEC));
	SkyblockerPredicateType<HeldByArmorStandPredicate> HELD_BY_ARMOR_STAND = register(HeldByArmorStandPredicate.ID, new SkyblockerPredicateType<>(HeldByArmorStandPredicate.CODEC));
	SkyblockerPredicateType<InsideScreenPredicate> INSIDE_SCREEN = register(InsideScreenPredicate.ID, new SkyblockerPredicateType<>(InsideScreenPredicate.CODEC));
	SkyblockerPredicateType<ItemPredicate> ITEM = register(ItemPredicate.ID, new SkyblockerPredicateType<>(ItemPredicate.CODEC));
	SkyblockerPredicateType<LocationPredicate> LOCATION = register(LocationPredicate.ID, new SkyblockerPredicateType<>(LocationPredicate.CODEC));
	SkyblockerPredicateType<LorePredicate> LORE = register(LorePredicate.ID, new SkyblockerPredicateType<>(LorePredicate.CODEC));
	SkyblockerPredicateType<NamePredicate> NAME = register(NamePredicate.ID, new SkyblockerPredicateType<>(NamePredicate.CODEC));
	SkyblockerPredicateType<NotPredicate> NOT = register(NotPredicate.ID, new SkyblockerPredicateType<>(NotPredicate.CODEC));
	SkyblockerPredicateType<OrPredicate> OR = register(OrPredicate.ID, new SkyblockerPredicateType<>(OrPredicate.CODEC));
	SkyblockerPredicateType<PetInfoPredicate> PET_INFO = register(PetInfoPredicate.ID, new SkyblockerPredicateType<>(PetInfoPredicate.CODEC));
	SkyblockerPredicateType<ProfileComponentPredicate> PROFILE_COMPONENT = register(ProfileComponentPredicate.ID, new SkyblockerPredicateType<>(ProfileComponentPredicate.CODEC));

	//Trigger class loading before the other STP classes to ensure the registry has everything registered before it could ever possibly be used
	@Init(priority = -1)
	static void init() {
	}

	static <T extends SkyblockerTexturePredicate> SkyblockerPredicateType<T> register(Identifier id, SkyblockerPredicateType<T> predicateType) {
		return Registry.register(SkyblockerPredicateType.REGISTRY, id, predicateType);
	}
}
