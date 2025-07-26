package de.hysky.skyblocker.skyblock.garden;

import static java.util.Map.entry;
import java.util.Map;

import de.hysky.skyblocker.skyblock.item.HeadTextures;

public interface GardenConstants {
	Map<String, String> CROP_BY_PEST = Map.ofEntries(
			entry("Fly", "Wheat"),
			entry("Mosquito", "Sugar Cane"),
			entry("Cricket", "Carrot"),
			entry("Locust", "Potato"),
			entry("Earthworm", "Melon"),
			entry("Rat", "Pumpkin"),
			entry("Moth", "Cocoa Beans"),
			entry("Beetle", "Nether Wart"),
			entry("Mite", "Cactus"),
			entry("Slug", "Mushroom"));

	Map<String, String> PEST_HEAD_BY_CROP = Map.ofEntries(
			entry("Wheat", HeadTextures.FLY_PEST),
			entry("Sugar Cane", HeadTextures.MOSQUITO_PEST),
			entry("Carrot", HeadTextures.CRICKET_PEST),
			entry("Potato", HeadTextures.LOCUST_PEST),
			entry("Melon", HeadTextures.EARTHWORM_PEST),
			entry("Pumpkin", HeadTextures.RAT_PEST),
			entry("Cocoa Beans", HeadTextures.MOTH_PEST),
			entry("Nether Wart", HeadTextures.BEETLE_PEST),
			entry("Cactus", HeadTextures.MITE_PEST),
			entry("Mushroom", HeadTextures.SLUG_PEST)
	);
}
