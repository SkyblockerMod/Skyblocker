package de.hysky.skyblocker.skyblock.garden;

import static java.util.Map.entry;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.hysky.skyblocker.skyblock.item.HeadTextures;
import net.minecraft.util.Util;

public interface GardenConstants {
	Map<String, String> CROP_BY_PEST = Map.ofEntries(
			entry("Fly", "Wheat"),
			entry("Mosquito", "Sugar Cane"),
			entry("Cricket", "Carrot"),
			entry("Locust", "Potato"),
			entry("Earthworm", "Melon Slice"),
			entry("Rat", "Pumpkin"),
			entry("Moth", "Cocoa Beans"),
			entry("Beetle", "Nether Wart"),
			entry("Mite", "Cactus"),
			entry("Slug", "Mushroom"),
			entry("Firefly", "Moonflower"),
			entry("Dragonfly", "Sunflower"),
			entry("Praying Mantis", "Wild Rose")
	);

	Multimap<String, String> PEST_HEAD_BY_CROP = Util.make(ArrayListMultimap.create(), map -> {
			map.put("Wheat", HeadTextures.FLY_PEST);
			map.put("Sugar Cane", HeadTextures.MOSQUITO_PEST);
			map.put("Carrot", HeadTextures.CRICKET_PEST);
			map.put("Potato", HeadTextures.LOCUST_PEST);
			map.put("Melon Slice", HeadTextures.EARTHWORM_PEST);
			map.put("Melon Slice", HeadTextures.EARTHWORM_PEST_TAIL);
			map.put("Pumpkin", HeadTextures.RAT_PEST);
			map.put("Cocoa Beans", HeadTextures.MOTH_PEST);
			map.put("Nether Wart", HeadTextures.BEETLE_PEST);
			map.put("Cactus", HeadTextures.MITE_PEST);
			map.put("Mushroom", HeadTextures.SLUG_PEST);
			map.put("Moonflower", HeadTextures.FIREFLY_PEST);
			map.put("Moonflower", HeadTextures.FIREFLY_PEST_FLASH);
			map.put("Sunflower", HeadTextures.DRAGONFLY_PEST);
			map.put("Wild Rose", HeadTextures.PRAYING_MANTIS_PEST);
	});

	Map<String, String> CROP_BY_VINYL = Map.ofEntries(
			entry("VINYL_PRETTY_FLY", "Wheat"),
			entry("VINYL_BUZZIN_BEATS", "Sugar Cane"),
			entry("VINYL_CRICKET_CHOIR", "Carrot"),
			entry("VINYL_CICADA_SYMPHONY", "Potato"),
			entry("VINYL_EARTHWORM_ENSEMBLE", "Melon Slice"),
			entry("VINYL_RODENT_REVOLUTION", "Pumpkin"),
			entry("VINYL_WINGS_OF_HARMONY", "Cocoa Beans"),
			entry("VINYL_BEETLE", "Nether Wart"),
			entry("VINYL_DYNAMITES", "Cactus"),
			entry("VINYL_SLOW_AND_GROOVY", "Mushroom"),
			entry("VINYL_FIREFLY", "Moonflower"),
			entry("VINYL_IMAGINE_DRAGONFLIES", "Sunflower"),
			entry("VINYL_PRAY_FOR_ME", "Wild Rose")
	);
}
