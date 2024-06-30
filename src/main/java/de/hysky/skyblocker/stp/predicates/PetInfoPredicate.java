package de.hysky.skyblocker.stp.predicates;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.PetCache.PetInfo;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

/**
 * Matches the {@code petInfo} field on pets.
 * 
 * @since 1.22.0
 */
public record PetInfoPredicate(Optional<String> type, Optional<String> tier) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "pet_info");
	public static final MapCodec<PetInfoPredicate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.optionalFieldOf("type").forGetter(PetInfoPredicate::type),
			Codec.STRING.optionalFieldOf("tier").forGetter(PetInfoPredicate::tier))
			.apply(instance, PetInfoPredicate::new));
	public static final Codec<PetInfoPredicate> CODEC = MAP_CODEC.codec();

	@Override
	public boolean test(ItemStack stack) {
		if (stack.contains(DataComponentTypes.CUSTOM_DATA)) {
			@SuppressWarnings("deprecation") //Avoid copying nbt for performance since we only want to read it
			NbtCompound customData = stack.get(DataComponentTypes.CUSTOM_DATA).getNbt();

			if (customData.contains("petInfo", NbtElement.STRING_TYPE)) {
				try {
					JsonElement serializedPetInfo = JsonParser.parseString(customData.getString("petInfo"));
					PetInfo info = PetCache.PetInfo.CODEC.parse(JsonOps.INSTANCE, serializedPetInfo).getOrThrow();

					boolean typeMatches = type.isPresent() ? info.type().equals(this.type.get()) : false;
					boolean tierMatches = tier.isPresent() ? info.tier().equals(this.tier.get()) : false;

					//Perform logical matching on the predicate
					//if type and tier are present, match both - if type or is tier present, match either - otherwise false
					return type.isPresent() && tier.isPresent() ? typeMatches && tierMatches : (type.isPresent() || tier.isPresent() ? typeMatches || tierMatches : false);
				} catch (Exception ignored) {}
			}
		}

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.PET_INFO;
	}
}
