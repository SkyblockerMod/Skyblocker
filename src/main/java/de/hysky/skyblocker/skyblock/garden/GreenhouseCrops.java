package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

public final class GreenhouseCrops {
	public static final Map<String, Crop> CROP_ID_MAP;
	public static final Map<Integer, Crop> CROP_BY_INT;
	public static final Map<String, Crop> CROP_BY_HEAD_TEXTURE_HASH;

	private GreenhouseCrops() {
	}

	static {
		Crop[] crops = {
			new Crop("Ashwreath", "ashwreath", 1, HeadTextures.ASHWREATH),
			new Crop("Choconut", "Choconut", 2, HeadTextures.CHOCONUT),
			new Crop("Dustgrain", "Dustgrain", 3, HeadTextures.DUSTGRAIN),
			new Crop("Gloomgourd", "Gloomgourd", 4, HeadTextures.GLOOMGOURD),
			new Crop("Lonelily", "Lonelilly", 5, HeadTextures.LONELILY),
			new Crop("Scourroot", "Scourroot", 6, HeadTextures.SCOURROOT),
			new Crop("Shadevine", "shadevine", 7, HeadTextures.SHADEVINE),
			new Crop("Veilshroom", "Veilshroom", 8, HeadTextures.VEILSHROOM),
			new Crop("Witherbloom", "Witherbloom", 9, HeadTextures.WITHERBLOOM),
			new Crop("Chocoberry", "Chocoberry", 10, HeadTextures.CHOCOBERRY),
			new Crop("Cindershade", "Cindershade", 11, HeadTextures.CINDERSHADE),
			new Crop("Coalroot", "Coalroot", 12, HeadTextures.COALROOT),
			new Crop("Creambloom", "Creambloom", 13, HeadTextures.CREAMBLOOM),
			new Crop("Duskbloom", "duskbloom", 14, HeadTextures.DUSKBLOOM),
			new Crop("Thornshade", "thornshade", 15, HeadTextures.THORNSHADE),
			new Crop("Blastberry", "blastberry", 16, HeadTextures.BLASTBERRY),
			new Crop("Cheesebite", "cheesebite", 17, HeadTextures.CHEESEBITE),
			new Crop("Chloronite", "chloronite", 18, HeadTextures.CHLORONITE),
			new Crop("Do Not Eat Shroom", "donoteatshroom", 19, HeadTextures.DO_NOT_EAT_SHROOM),
			new Crop("Fleshtrap", "fleshtrap", 20, HeadTextures.FLESHTRAP),
			new Crop("Magic Jellybean", "magicjellybean", 21, HeadTextures.MAGIC_JELLYBEAN),
			new Crop("Noctilume", "noctilume", 22, HeadTextures.NOCTILUME),
			new Crop("Snoozling", "snoozlingFlower", 23, HeadTextures.SNOOZLING), // requires special handling, only look for head
			new Crop("Soggybud", "soggybud", 24, HeadTextures.SOGGYBUD),
			new Crop("Chorus Fruit", "chorusFruit", 25, HeadTextures.CHORUS_FRUIT),
			new Crop("PlantBoy Advance", "Plantboy", 26, HeadTextures.PLANTBOY_ADVANCE), // plantboyroot is also a thing. also is 2x2, so adjust accordingly
			new Crop("Puffercloud", "puffercloud", 27, HeadTextures.PUFFERCLOUD),
			new Crop("Shellfruit", "shellfruit", 28, HeadTextures.SHELLFRUIT),
			new Crop("Startlevine", "startlevine", 29, HeadTextures.STARTLEVINE),
			new Crop("Stoplight Petal", "stoplightpetal", 30, HeadTextures.STOPLIGHT_PETAL),
			new Crop("Thunderling", "thunderling", 31, HeadTextures.THUNDERLING),
			new Crop("Turtlellini", "turtlellini", 32, HeadTextures.TURTLELLINI),
			new Crop("Zombud", "zombud", 33, HeadTextures.ZOMBUD),
			new Crop("All In Aloe", "allinaloe", 34, HeadTextures.ALL_IN_ALOE),
			new Crop("Devourer", "devourer", 35, HeadTextures.DEVOURER),
			new Crop("Glasscorn", "glasscorn", 36, HeadTextures.GLASSCORN),
			new Crop("Godseed", "godseed", 37, HeadTextures.GODSEED), // also has godseedpillar 's around it
			new Crop("Jerryflower", "jerryseed", 38, HeadTextures.JERRYFLOWER), // weird naming??
			new Crop("Phantomleaf", "phantomleaf", 39, HeadTextures.PHANTOMLEAF),
			new Crop("Timestalk", "timestalk", 40, HeadTextures.TIMESTALK), // verify

			// Crops
			new Crop("Cocoa Beans", "coco", 41, HeadTextures.COCOA_BEANS),
			new Crop("Wild Rose", "wildrose", 42, HeadTextures.WILD_ROSE),
			new Crop("Moonflower", "moonflower", 43, HeadTextures.MOONFLOWER),
			new Crop("Sunflower", "sunflower", 44, HeadTextures.SUNFLOWER),
			new Crop("Cactus", "cactus", 45, HeadTextures.CACTUS),
			new Crop("Melon Seeds", "melon", 46, HeadTextures.MELON),
			new Crop("Pumpkin Seeds", "pumpkin", 47, HeadTextures.PUMPKIN),
			new Crop("Brown Mushroom", "brownmushroom", 48, HeadTextures.BROWN_MUSHROOM), // TODO: make mushrooms interchangeable, also replace with head texture
			new Crop("Red Mushroom", "redmushroom", 49, HeadTextures.RED_MUSHROOM),
			new Crop("Wheat Seeds", "wheat", 50, Blocks.WHEAT),
			new Crop("Carrot", "carrot", 51, Blocks.CARROTS),
			new Crop("Potato", "potato", 52, Blocks.POTATOES),
			new Crop("Nether Wart", "netherwart", 53, Blocks.NETHER_WART),
			new Crop("Sugar Cane", "sugarcane", 54, Blocks.SUGAR_CANE),

			// Misc
			new Crop("Dead Plant", "deadplant", 55, Blocks.DEAD_BUSH),
			new Crop("Fire", "fire", 56, Blocks.FIRE),

			// Special crops
			new Crop("Helianthus", "helianthus", 57, HeadTextures.HELIANTHUS),
			new Crop("Fermento", "fermento", 58, HeadTextures.FERMENTO),
			new Crop("Squash", "squash", 59, HeadTextures.SQUASH),
			new Crop("Cropie", "cropie", 60, HeadTextures.CROPIE),
		};

		CROP_ID_MAP = Map.ofEntries(Stream.of(crops).map(c -> Map.entry(c.name(), c)).toArray(Map.Entry[]::new));
		CROP_BY_INT = Map.ofEntries(Stream.of(crops).map(c -> Map.entry(c.id(), c)).toArray(Map.Entry[]::new));
		CROP_BY_HEAD_TEXTURE_HASH = Map.ofEntries(Stream.of(crops).filter(Crop::isHead).map(c -> Map.entry(c.headSkin(), c)).toArray(Map.Entry[]::new));
	}

	public record Crop(
		String name,
		String armorStandName,
		int id,
		boolean isHead,
		@Nullable Block cropBlock,
		@Nullable String headSkin,
		@Nullable FlexibleItemStack displayStack
	) {
		public Crop(String name, String armorStandName, int id, String headSkin) {
			this(name, armorStandName, id, true, null, headSkin, ItemUtils.createSkull(headSkin));
		}

		public Crop(String name, String armorStandName, int id, Block cropBlock) {
			this(name, armorStandName, id, false, cropBlock, null, null);
		}
	}
}
