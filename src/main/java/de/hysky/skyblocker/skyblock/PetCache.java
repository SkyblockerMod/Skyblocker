package de.hysky.skyblocker.skyblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;

/**
 * Doesn't work with auto pet right now because thats complicated.
 * <p>
 * Want support? Ask the Admins for a Mod API event or open your pets menu.
 */
public class PetCache {
    public record PetInfo(String type, double exp, String tier, Optional<String> uuid, Optional<String> item, Optional<String> skin) {
        // TODO: Combine with SkyblockItemRarity
        private static final String[] TIER_INDEX = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"};

        public static final Codec<PetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(PetInfo::type),
                Codec.DOUBLE.fieldOf("exp").forGetter(PetInfo::exp),
                Codec.STRING.fieldOf("tier").forGetter(PetInfo::tier),
                Codec.STRING.optionalFieldOf("uuid").forGetter(PetInfo::uuid),
                Codec.STRING.optionalFieldOf("heldItem").forGetter(PetInfo::item),
                Codec.STRING.optionalFieldOf("skin").forGetter(PetInfo::skin)
        ).apply(instance, PetInfo::new));
        private static final Codec<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PetInfo>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING,
                Codec.unboundedMap(Codec.STRING, CODEC).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new)
        ).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);

        public int tierIndex() {
            return ArrayUtils.indexOf(TIER_INDEX, tier);
        }
    }
}