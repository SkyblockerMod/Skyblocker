package de.hysky.skyblocker.skyblock.crimson.kuudra;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.skyblock.crimson.CrimsonFaction;
import de.hysky.skyblocker.skyblock.item.PetInfo;

/**
 * Stores profile-specific data related to Kuudra.
 *
 * @param factio          The player's selected faction in the Crimson Isle.
 * @param kuudraPet       The player's Kuudra pet.
 * @param kuudraKeyPrices The cost for Kuudra keys, note it is **not** the same for all players despite the Wiki's claims.
 */
public record KuudraProfileData(CrimsonFaction faction, PetInfo kuudraPet, Map<String, Integer> kuudraKeyPrices) {
	public static final Codec<KuudraProfileData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CrimsonFaction.CODEC.fieldOf("faction").forGetter(KuudraProfileData::faction),
			PetInfo.CODEC.fieldOf("kuudraPet").forGetter(KuudraProfileData::kuudraPet),
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("kuudraKeyPrices").forGetter(KuudraProfileData::kuudraKeyPrices)
			).apply(instance, KuudraProfileData::new));
	public static final KuudraProfileData EMPTY = new KuudraProfileData(CrimsonFaction.MAGE, PetInfo.EMPTY, Map.of(
			"KUUDRA_TIER_KEY", 200_000,
			"KUUDRA_HOT_TIER_KEY", 400_000,
			"KUUDRA_BURNING_TIER_KEY", 750_000,
			"KUUDRA_FIERY_TIER_KEY", 1_500_000,
			"KUUDRA_INFERNAL_TIER_KEY", 3_000_000
			));
}
