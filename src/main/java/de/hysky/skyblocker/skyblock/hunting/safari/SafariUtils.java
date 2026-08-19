package de.hysky.skyblocker.skyblock.hunting.safari;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;

import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.SkyBlockBiomes;
import de.hysky.skyblocker.utils.Utils;

import static java.util.Map.entry;

public class SafariUtils {
	public enum Critters {
		// Cavern
		CAVERNFISH,
		FLITTER,
		SHYWORM,
		DRIFTLING,
		CHUCKWALLA,
		ROCKMITE,
		SCRAPPY,
		SNOOZLE,
		GEMZIE,
		// Forest
		FOXTROT,
		BLUEBIRD,
		HONEYBUG,
		TREEFROG,
		WOODCHUCKER,
		FLUFFLING,
		HIDEONFLOOR,
		PARAKEET,
		MACAW,
		// Haunted
		AREITA,
		BLOODBAT,
		DUPLICO,
		GAZER,
		LITTERBUG,
		SOLSNATCHER,
		GIMMIEGOLD,
		HIDEONWALL,
		HIDEYHO,
		DOOMSPIRAL,
		// Icy
		STRONGARM,
		TEPID,
		POLARIS,
		SHUDDERSQUID,
		BILLYGOAT,
		MANTIS_SHRIMP,
		NOZZLENOSE,
		TROODON,
		WUMPA
	}

	public enum CritterCount {
		FIXED,
		RANGE,
		RANDOM
	}

	public record CritterDetails(CritterCount count, FlexibleItemStack head, SkyblockItemRarity rarity, Integer min, @Nullable Integer max) {
		public static CritterDetails ofFixed(String texture, SkyblockItemRarity rarity, int amount) {
			return new CritterDetails(CritterCount.FIXED, ItemUtils.createSkull(texture), rarity, amount, null);
		}

		public static CritterDetails ofRange(String texture, SkyblockItemRarity rarity, int min, int max) {
			return new CritterDetails(CritterCount.RANGE, ItemUtils.createSkull(texture), rarity, min, max);
		}

		public static CritterDetails ofRandom(String texture, SkyblockItemRarity rarity) {
			return new CritterDetails(CritterCount.RANDOM, ItemUtils.createSkull(texture), rarity, 0, null);
		}
	}

	// Biomes for every critter
	public static final EnumSet<Critters> CAVERN_CRITTERS = EnumSet.of(
			Critters.CAVERNFISH, Critters.FLITTER, Critters.SHYWORM, Critters.DRIFTLING, Critters.CHUCKWALLA, Critters.ROCKMITE, Critters.SCRAPPY, Critters.SNOOZLE, Critters.GEMZIE
	);
	public static final EnumSet<Critters> FOREST_CRITTERS = EnumSet.of(
			Critters.FOXTROT, Critters.BLUEBIRD, Critters.HONEYBUG, Critters.TREEFROG, Critters.WOODCHUCKER, Critters.FLUFFLING, Critters.HIDEONFLOOR, Critters.PARAKEET, Critters.MACAW
	);
	public static final EnumSet<Critters> HAUNTED_CRITTERS = EnumSet.of(
			Critters.AREITA, Critters.BLOODBAT, Critters.DUPLICO, Critters.GAZER, Critters.LITTERBUG, Critters.SOLSNATCHER, Critters.GIMMIEGOLD, Critters.HIDEONWALL, Critters.HIDEYHO,  Critters.DOOMSPIRAL
	);
	public static final EnumSet<Critters> ICY_CRITTERS = EnumSet.of(
			Critters.STRONGARM, Critters.TEPID, Critters.POLARIS, Critters.SHUDDERSQUID, Critters.BILLYGOAT, Critters.MANTIS_SHRIMP, Critters.NOZZLENOSE, Critters.TROODON, Critters.WUMPA
	);

	// Spawn count details for every critter
	public static final Map<Critters, CritterDetails> CRITTER_DETAILS = new EnumMap<>(Map.ofEntries(
			// Cavern
			entry(Critters.CAVERNFISH, CritterDetails.ofRange(HeadTextures.CAVERNFISH_CRITTER, SkyblockItemRarity.COMMON, 4, 8)),
			entry(Critters.FLITTER, CritterDetails.ofRange(HeadTextures.FLITTER_CRITTER, SkyblockItemRarity.COMMON, 6, 8)),
			entry(Critters.SHYWORM, CritterDetails.ofRange(HeadTextures.SHYWORM_CRITTER, SkyblockItemRarity.COMMON, 4, 8)),
			entry(Critters.DRIFTLING, CritterDetails.ofRange(HeadTextures.DRIFTLING_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.CHUCKWALLA, CritterDetails.ofRange(HeadTextures.CHUCKWALLA_CRITTER, SkyblockItemRarity.RARE, 2, 4)),
			entry(Critters.ROCKMITE, CritterDetails.ofRandom(HeadTextures.ROCKMITE_CRITTER, SkyblockItemRarity.RARE)),
			entry(Critters.SCRAPPY, CritterDetails.ofFixed(HeadTextures.SCRAPPY_CRITTER, SkyblockItemRarity.RARE, 3)),
			entry(Critters.SNOOZLE, CritterDetails.ofRandom(HeadTextures.SNOOZLE_CRITTER, SkyblockItemRarity.RARE)),
			entry(Critters.GEMZIE, CritterDetails.ofFixed(HeadTextures.GEMZIE_CRITTER, SkyblockItemRarity.EPIC, 3)),
			// Forest
			entry(Critters.FOXTROT, CritterDetails.ofRange(HeadTextures.FOXTROT_CRITTER, SkyblockItemRarity.COMMON, 6, 8)),
			entry(Critters.BLUEBIRD, CritterDetails.ofRandom(HeadTextures.BLUEBIRD_CRITTER, SkyblockItemRarity.COMMON)),
			entry(Critters.HONEYBUG, CritterDetails.ofRange(HeadTextures.HONEYBUG_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.TREEFROG, CritterDetails.ofRange(HeadTextures.TREEFROG_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.WOODCHUCKER, CritterDetails.ofRange(HeadTextures.WOODCHUCKER_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.FLUFFLING, CritterDetails.ofRange(HeadTextures.FLUFFLING_CRITTER, SkyblockItemRarity.RARE, 1, 3)),
			entry(Critters.HIDEONFLOOR, CritterDetails.ofRange(HeadTextures.HIDEONFLOOR_CRITTER, SkyblockItemRarity.RARE, 1, 3)),
			entry(Critters.PARAKEET, CritterDetails.ofRandom(HeadTextures.PARAKEET_CRITTER, SkyblockItemRarity.RARE)),
			entry(Critters.MACAW, CritterDetails.ofRandom(HeadTextures.MACAW_CRITTER, SkyblockItemRarity.LEGENDARY)),
			// Haunted
			entry(Critters.AREITA, CritterDetails.ofRange(HeadTextures.AREITA_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.BLOODBAT, CritterDetails.ofRange(HeadTextures.BLOODBAT_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.DUPLICO, CritterDetails.ofRange(HeadTextures.DUPLICO_CRITTER, SkyblockItemRarity.UNCOMMON, 2, 4)),
			entry(Critters.GAZER, CritterDetails.ofFixed(HeadTextures.GAZER_CRITTER, SkyblockItemRarity.UNCOMMON, 4)),
			entry(Critters.LITTERBUG, CritterDetails.ofRange(HeadTextures.LITTERBUG_CRITTER, SkyblockItemRarity.UNCOMMON, 4, 8)),
			entry(Critters.SOLSNATCHER, CritterDetails.ofRange(HeadTextures.SOLSNATCHER_CRITTER, SkyblockItemRarity.UNCOMMON, 4, 8)),
			// NOT correct but works for now, real min and max likely depends on floor drop logic
			entry(Critters.GIMMIEGOLD, CritterDetails.ofRandom(HeadTextures.GIMMIEGOLD_CRITTER, SkyblockItemRarity.RARE)),
			entry(Critters.HIDEONWALL, CritterDetails.ofRange(HeadTextures.HIDEONWALL_CRITTER, SkyblockItemRarity.RARE, 2, 4)),
			entry(Critters.HIDEYHO, CritterDetails.ofFixed(HeadTextures.HIDEYHO_CRITTER, SkyblockItemRarity.RARE, 1)),
			entry(Critters.DOOMSPIRAL, CritterDetails.ofFixed(HeadTextures.DOOMSPIRAL_CRITTER, SkyblockItemRarity.LEGENDARY, 1)),
			// Icy
			entry(Critters.STRONGARM, CritterDetails.ofRange(HeadTextures.STRONGARM_CRITTER, SkyblockItemRarity.COMMON, 4, 8)),
			entry(Critters.TEPID, CritterDetails.ofRange(HeadTextures.TEPID_CRITTER, SkyblockItemRarity.COMMON, 6, 8)),
			entry(Critters.POLARIS, CritterDetails.ofRange(HeadTextures.POLARIS_CRITTER, SkyblockItemRarity.UNCOMMON, 2, 4)),
			entry(Critters.SHUDDERSQUID, CritterDetails.ofRange(HeadTextures.SHUDDERSQUID_CRITTER, SkyblockItemRarity.UNCOMMON, 3, 6)),
			entry(Critters.BILLYGOAT, CritterDetails.ofRange(HeadTextures.BILLYGOAT_CRITTER, SkyblockItemRarity.RARE, 2, 4)),
			entry(Critters.MANTIS_SHRIMP, CritterDetails.ofRange(HeadTextures.MANTIS_SHRIMP_CRITTER, SkyblockItemRarity.RARE, 3, 6)),
			entry(Critters.NOZZLENOSE, CritterDetails.ofRange(HeadTextures.NOZZLENOSE_CRITTER, SkyblockItemRarity.RARE, 2, 4)),
			entry(Critters.TROODON, CritterDetails.ofFixed(HeadTextures.TROODON_CRITTER, SkyblockItemRarity.RARE, 3)),
			entry(Critters.WUMPA, CritterDetails.ofFixed(HeadTextures.WUMPA_CRITTER, SkyblockItemRarity.LEGENDARY, 1))
	));

	// Center blocks of every snoozle wall
	public static final ArrayList<BlockPos> SNOOZLE_WALL_CORES = new ArrayList<>(Arrays.asList(
			new BlockPos(-96, 43, 17),
			new BlockPos(-95, 43, 42),
			new BlockPos(-70, 42, 68),
			new BlockPos(-114, 42, 87),
			new BlockPos(-126, 42, 74)
	));

	private static final ArrayList<BlockPosSet> SNOOZLE_WALLS = new ArrayList<>();

	public static ArrayList<BlockPosSet> getSnoozleWalls() {
		if (!SNOOZLE_WALLS.isEmpty()) return SNOOZLE_WALLS;

		var firstWall = new BlockPosSet();
		firstWall.addXYZ(-97, 46, 17);
		firstWall.addXYZ(-96, 46, 17);
		firstWall.addXYZ(-98, 45, 17);
		firstWall.addXYZ(-97, 45, 17);
		firstWall.addXYZ(-96, 45, 17);
		firstWall.addXYZ(-95, 45, 17);
		firstWall.addXYZ(-94, 45, 17);
		firstWall.addXYZ(-98, 44, 17);
		firstWall.addXYZ(-97, 44, 17);
		firstWall.addXYZ(-96, 44, 17);
		firstWall.addXYZ(-95, 44, 17);
		firstWall.addXYZ(-94, 44, 17);
		firstWall.addXYZ(-93, 44, 17);
		firstWall.addXYZ(-99, 43, 17);
		firstWall.addXYZ(-98, 43, 17);
		firstWall.addXYZ(-97, 43, 17);
		firstWall.addXYZ(-96, 43, 17);
		firstWall.addXYZ(-95, 43, 17);
		firstWall.addXYZ(-94, 43, 17);
		firstWall.addXYZ(-93, 43, 17);
		firstWall.addXYZ(-99, 42, 17);
		firstWall.addXYZ(-98, 42, 17);
		firstWall.addXYZ(-97, 42, 17);
		firstWall.addXYZ(-96, 42, 17);
		firstWall.addXYZ(-95, 42, 17);
		firstWall.addXYZ(-94, 42, 17);
		firstWall.addXYZ(-99, 41, 17);
		firstWall.addXYZ(-98, 41, 17);
		firstWall.addXYZ(-97, 41, 17);
		firstWall.addXYZ(-96, 41, 17);
		firstWall.addXYZ(-95, 41, 17);
		firstWall.addXYZ(-94, 41, 17);
		firstWall.addXYZ(-98, 40, 17);
		firstWall.addXYZ(-97, 40, 17);
		firstWall.addXYZ(-96, 40, 17);
		firstWall.addXYZ(-95, 40, 17);
		SNOOZLE_WALLS.add(firstWall);

		var secondWall = new BlockPosSet();
		secondWall.addXYZ(-94, 46, 41);
		secondWall.addXYZ(-95, 46, 42);
		secondWall.addXYZ(-94, 45, 41);
		secondWall.addXYZ(-95, 45, 42);
		secondWall.addXYZ(-96, 45, 43);
		secondWall.addXYZ(-93, 44, 40);
		secondWall.addXYZ(-94, 44, 41);
		secondWall.addXYZ(-95, 44, 42);
		secondWall.addXYZ(-96, 44, 43);
		secondWall.addXYZ(-93, 43, 40);
		secondWall.addXYZ(-94, 43, 41);
		secondWall.addXYZ(-95, 43, 42);
		secondWall.addXYZ(-96, 43, 43);
		secondWall.addXYZ(-97, 43, 44);
		secondWall.addXYZ(-93, 42, 40);
		secondWall.addXYZ(-94, 42, 41);
		secondWall.addXYZ(-95, 42, 42);
		secondWall.addXYZ(-96, 42, 43);
		secondWall.addXYZ(-97, 42, 44);
		secondWall.addXYZ(-93, 41, 40);
		secondWall.addXYZ(-94, 41, 41);
		secondWall.addXYZ(-95, 41, 42);
		secondWall.addXYZ(-96, 41, 43);
		secondWall.addXYZ(-94, 40, 41);
		secondWall.addXYZ(-95, 40, 42);
		secondWall.addXYZ(-96, 40, 43);
		SNOOZLE_WALLS.add(secondWall);

		var thirdWall = new BlockPosSet();
		thirdWall.addXYZ(-69, 45, 67);
		thirdWall.addXYZ(-70, 45, 68);
		thirdWall.addXYZ(-69, 44, 67);
		thirdWall.addXYZ(-70, 44, 68);
		thirdWall.addXYZ(-71, 44, 69);
		thirdWall.addXYZ(-68, 43, 66);
		thirdWall.addXYZ(-69, 43, 67);
		thirdWall.addXYZ(-70, 43, 68);
		thirdWall.addXYZ(-71, 43, 69);
		thirdWall.addXYZ(-68, 42, 66);
		thirdWall.addXYZ(-69, 42, 67);
		thirdWall.addXYZ(-70, 42, 68);
		thirdWall.addXYZ(-71, 42, 69);
		thirdWall.addXYZ(-72, 42, 70);
		thirdWall.addXYZ(-68, 41, 66);
		thirdWall.addXYZ(-69, 41, 67);
		thirdWall.addXYZ(-70, 41, 68);
		thirdWall.addXYZ(-71, 41, 69);
		thirdWall.addXYZ(-72, 41, 70);
		thirdWall.addXYZ(-68, 40, 66);
		thirdWall.addXYZ(-69, 40, 67);
		thirdWall.addXYZ(-70, 40, 68);
		thirdWall.addXYZ(-71, 40, 69);
		thirdWall.addXYZ(-69, 39, 67);
		thirdWall.addXYZ(-70, 39, 68);
		thirdWall.addXYZ(-71, 39, 69);
		SNOOZLE_WALLS.add(thirdWall);

		var fourthWall = new BlockPosSet();
		fourthWall.addXYZ(-113, 45, 87);
		fourthWall.addXYZ(-114, 45, 87);
		fourthWall.addXYZ(-112, 44, 87);
		fourthWall.addXYZ(-113, 44, 87);
		fourthWall.addXYZ(-114, 44, 87);
		fourthWall.addXYZ(-115, 44, 87);
		fourthWall.addXYZ(-116, 44, 87);
		fourthWall.addXYZ(-112, 43, 87);
		fourthWall.addXYZ(-113, 43, 87);
		fourthWall.addXYZ(-114, 43, 87);
		fourthWall.addXYZ(-115, 43, 87);
		fourthWall.addXYZ(-116, 43, 87);
		fourthWall.addXYZ(-117, 43, 87);
		fourthWall.addXYZ(-111, 42, 87);
		fourthWall.addXYZ(-112, 42, 87);
		fourthWall.addXYZ(-113, 42, 87);
		fourthWall.addXYZ(-114, 42, 87);
		fourthWall.addXYZ(-115, 42, 87);
		fourthWall.addXYZ(-116, 42, 87);
		fourthWall.addXYZ(-117, 42, 87);
		fourthWall.addXYZ(-111, 41, 87);
		fourthWall.addXYZ(-112, 41, 87);
		fourthWall.addXYZ(-113, 41, 87);
		fourthWall.addXYZ(-114, 41, 87);
		fourthWall.addXYZ(-115, 41, 87);
		fourthWall.addXYZ(-116, 41, 87);
		fourthWall.addXYZ(-111, 40, 87);
		fourthWall.addXYZ(-112, 40, 87);
		fourthWall.addXYZ(-113, 40, 87);
		fourthWall.addXYZ(-114, 40, 87);
		fourthWall.addXYZ(-115, 40, 87);
		fourthWall.addXYZ(-116, 40, 87);
		fourthWall.addXYZ(-112, 39, 87);
		fourthWall.addXYZ(-113, 39, 87);
		fourthWall.addXYZ(-114, 39, 87);
		fourthWall.addXYZ(-115, 39, 87);
		SNOOZLE_WALLS.add(fourthWall);

		var fifthWall = new BlockPosSet();
		fifthWall.addXYZ(-125, 45, 75);
		fifthWall.addXYZ(-126, 45, 74);
		fifthWall.addXYZ(-125, 44, 75);
		fifthWall.addXYZ(-126, 44, 74);
		fifthWall.addXYZ(-127, 44, 73);
		fifthWall.addXYZ(-124, 43, 76);
		fifthWall.addXYZ(-125, 43, 75);
		fifthWall.addXYZ(-126, 43, 74);
		fifthWall.addXYZ(-127, 43, 73);
		fifthWall.addXYZ(-124, 42, 76);
		fifthWall.addXYZ(-125, 42, 75);
		fifthWall.addXYZ(-126, 42, 74);
		fifthWall.addXYZ(-127, 42, 73);
		fifthWall.addXYZ(-128, 42, 72);
		fifthWall.addXYZ(-124, 41, 76);
		fifthWall.addXYZ(-125, 41, 75);
		fifthWall.addXYZ(-126, 41, 74);
		fifthWall.addXYZ(-127, 41, 73);
		fifthWall.addXYZ(-128, 41, 72);
		fifthWall.addXYZ(-124, 40, 76);
		fifthWall.addXYZ(-125, 40, 75);
		fifthWall.addXYZ(-126, 40, 74);
		fifthWall.addXYZ(-127, 40, 73);
		fifthWall.addXYZ(-125, 39, 75);
		fifthWall.addXYZ(-126, 39, 74);
		fifthWall.addXYZ(-127, 39, 73);
		SNOOZLE_WALLS.add(fifthWall);

		return SNOOZLE_WALLS;
	}

	// Every possible honeybug nest spawn location
	public static ArrayList<BlockPos> HONEYBUG_HIVES = new ArrayList<>(Arrays.asList(
			new BlockPos(-22, 68, 39),
			new BlockPos(-17, 68, 41),
			new BlockPos(25, 70, 41),
			new BlockPos(23, 70, 46),
			new BlockPos(-1, 81, 60),
			new BlockPos(-9, 81, 58),
			new BlockPos(15, 88, 31),
			new BlockPos(20, 88, 33),
			new BlockPos(-1, 84, 9),
			new BlockPos(-13, 103, 21),
			new BlockPos(-11, 103, 13),
			new BlockPos(18, 114, 79),
			new BlockPos(16, 114, 87)
	));

	public static boolean isInCavernBiome() {
		return Utils.isInSafari() && Utils.isInBiome(SkyBlockBiomes.CAVERN);
	}

	public static boolean isInForestBiome() {
		return Utils.isInSafari() && Utils.isInBiome(SkyBlockBiomes.FOREST);
	}

	public static boolean isInHauntedBiome() {
		return Utils.isInSafari() && Utils.isInBiome(SkyBlockBiomes.HAUNTED);
	}

	public static boolean isInIcyBiome() {
		return Utils.isInSafari() && (Utils.isInBiome(SkyBlockBiomes.ICY) || Utils.isInBiome(SkyBlockBiomes.ICY_CAVES));
	}
}
