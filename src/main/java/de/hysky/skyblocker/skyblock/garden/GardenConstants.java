package de.hysky.skyblocker.skyblock.garden;

import static java.util.Map.entry;
import java.util.Map;

import net.minecraft.util.Util;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

	Multimap<String, String> PEST_HEAD_BY_CROP = Util.make(ArrayListMultimap.create(), map -> {
			map.put("Wheat", HeadTextures.FLY_PEST);
			map.put("Sugar Cane", HeadTextures.MOSQUITO_PEST);
			map.put("Carrot", HeadTextures.CRICKET_PEST);
			map.put("Potato", HeadTextures.LOCUST_PEST);
			map.put("Melon", HeadTextures.EARTHWORM_PEST);
			map.put("Melon", HeadTextures.EARTHWORM_PEST_TAIL);
			map.put("Pumpkin", HeadTextures.RAT_PEST);
			map.put("Cocoa Beans", HeadTextures.MOTH_PEST);
			map.put("Nether Wart", HeadTextures.BEETLE_PEST);
			map.put("Cactus", HeadTextures.MITE_PEST);
			map.put("Mushroom", HeadTextures.SLUG_PEST);
	});
}
