package de.hysky.skyblocker.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.datafixer.JsonHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;

public class ConfigDatafixer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

	public static void apply() {
		//User is new - has no config file (or maybe config folder)
		if (!Files.exists(CONFIG_DIR) || !Files.exists(CONFIG_DIR.resolve("skyblocker.json"))) return;

		//Should never be null if the file exists unless its malformed JSON or something in which case well it gets reset
		JsonObject oldConfig = loadConfig();
		if (oldConfig == null || JsonHelper.getInt(oldConfig, "version").orElse(1) != 1) return;

		try {
			JsonObject newConfig = prepareNewFormat();

			DataFixer[] generalFixers = getGeneralDataFixerRules();
			DataFixer[] uiAndVisualsDataFixers = getUIAndVisualsDataFixerRules();
			DataFixer[] dungeonsFixers = getDungeonsDataFixerRules();
			DataFixer[] helpersFixers = getHelpersDataFixerRules();
			DataFixer[] crimsonFixer = getCrimsonIsleDataFixerRule();
			DataFixer[] miningFixers = getMiningDataFixerRules();
			DataFixer[] farmingFixers = getFarmingDataFixerRules();
			DataFixer[] otherLocationsFixers = getOtherLocationsDataFixerRules();
			DataFixer[] slayersFixers = getSlayersDataFixerRules();
			DataFixer[] chatFixers = getChatDataFixerRules();
			DataFixer[] quickNavFixers = getQuickNavDataFixerRules();
			DataFixer[] miscFixers = getMiscDataFixerRules();

			//Combine into 1 array
			DataFixer[] fixers = Stream.of(generalFixers, uiAndVisualsDataFixers, dungeonsFixers, helpersFixers, crimsonFixer, miningFixers, farmingFixers, otherLocationsFixers, slayersFixers, chatFixers, quickNavFixers, miscFixers)
					.flatMap(Arrays::stream)
					.toArray(DataFixer[]::new);

			long start = System.currentTimeMillis();

			for (DataFixer fixer : fixers) {
				fixer.apply(oldConfig, newConfig);
			}

			//Write the updated file
			boolean success = writeConfig(CONFIG_DIR.resolve("skyblocker.json"), newConfig);

			if (!success) throw new IllegalStateException("Failed to write the new config to the file!");

			long end = System.currentTimeMillis();
			LOGGER.info("[Skyblocker Config Data Fixer] Applied {} datafixers in {} ms!", fixers.length, (end - start));
		} catch (Throwable t) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to fix up config file!", t);
			writeConfig(CONFIG_DIR.resolve("skyblocker-1.json"), oldConfig);
		}
	}

	private static JsonObject loadConfig() {
		try (BufferedReader reader = Files.newBufferedReader(CONFIG_DIR.resolve("skyblocker.json"))) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to load config file!", t);
		}

		return null;
	}

	private static boolean writeConfig(Path path, JsonObject config) {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			SkyblockerMod.GSON.toJson(config, writer);

			return true;
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to save config file at {}!", path, t);
		}

		return false;
	}

	private static JsonObject prepareNewFormat() {
		JsonObject root = new JsonObject();

		root.addProperty("version", 2);
		root.add("general", new JsonObject());
		root.add("uiAndVisuals", new JsonObject());
		root.add("helpers", new JsonObject());
		root.add("dungeons", new JsonObject());
		root.add("crimsonIsle", new JsonObject());
		root.add("mining", new JsonObject());
		root.add("farming", new JsonObject());
		root.add("otherLocations", new JsonObject());
		root.add("slayers", new JsonObject());
		root.add("chat", new JsonObject());
		root.add("quickNav", new JsonObject());
		root.add("misc", new JsonObject());

		return root;
	}

	private static DataFixer[] getGeneralDataFixerRules() {
		//Individual Fields
		DataFixer tips = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").addProperty("enableTips", JsonHelper.getBoolean(oldFmt, "general.enableTips").orElse(true));
		DataFixer acceptReparty = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("acceptReparty", oldFmt.getAsJsonObject("general").get("acceptReparty"));

		//Category Copies
		DataFixer shortcuts = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("shortcuts", oldFmt.getAsJsonObject("general").getAsJsonObject("shortcuts"));
		DataFixer quiverWarning = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("quiverWarning", oldFmt.getAsJsonObject("general").getAsJsonObject("quiverWarning"));
		DataFixer itemList = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("itemList", oldFmt.getAsJsonObject("general").getAsJsonObject("itemList"));
		DataFixer itemTooltip = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("itemTooltip", oldFmt.getAsJsonObject("general").getAsJsonObject("itemTooltip"));
		DataFixer itemInfoDisplay = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("itemInfoDisplay", oldFmt.getAsJsonObject("general").getAsJsonObject("itemInfoDisplay"));
		DataFixer itemProtection = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("itemProtection")) {
				newFmt.getAsJsonObject("general").add("itemProtection", oldFmt.getAsJsonObject("general").get("itemProtection"));
			}
		};		
		DataFixer wikiLookup = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("wikiLookup")) {
				newFmt.getAsJsonObject("general").add("wikiLookup", oldFmt.getAsJsonObject("general").get("wikiLookup"));
			}
		};
		DataFixer specialEffects = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("specialEffects", oldFmt.getAsJsonObject("general").getAsJsonObject("specialEffects"));
		DataFixer hitbox = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("hitbox", oldFmt.getAsJsonObject("general").getAsJsonObject("hitbox"));

		//Moved Field
		DataFixer dungeonQuality = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").getAsJsonObject("itemTooltip").addProperty("dungeonQuality", JsonHelper.getBoolean(oldFmt, "general.dungeonQuality").orElse(true));

		//Data stuff
		DataFixer lockedSlots = (oldFmt, newFmt) -> newFmt.getAsJsonObject("general").add("lockedSlots", oldFmt.getAsJsonObject("general").get("lockedSlots"));
		DataFixer protectedItems = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("protectedItems")) {
				newFmt.getAsJsonObject("general").add("protectedItems", oldFmt.getAsJsonObject("general").get("protectedItems"));
			}
		};
		DataFixer customItemNames = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("customItemNames")) {
				newFmt.getAsJsonObject("general").add("customItemNames", oldFmt.getAsJsonObject("general").get("customItemNames"));
			}
		};
		DataFixer customDyeColors = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("customDyeColors")) {
				newFmt.getAsJsonObject("general").add("customDyeColors", oldFmt.getAsJsonObject("general").get("customDyeColors"));
			}
		};
		DataFixer customArmorTrims = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("customArmorTrims")) {
				newFmt.getAsJsonObject("general").add("customArmorTrims", oldFmt.getAsJsonObject("general").get("customArmorTrims"));
			}
		};
		DataFixer customAnimatedDyes = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("customAnimatedDyes")) {
				newFmt.getAsJsonObject("general").add("customAnimatedDyes", oldFmt.getAsJsonObject("general").get("customAnimatedDyes"));
			}
		};

		return new DataFixer[] { tips, acceptReparty, shortcuts, quiverWarning, itemList, itemTooltip, itemInfoDisplay, itemProtection, wikiLookup, specialEffects, hitbox, dungeonQuality,
				lockedSlots, protectedItems, customItemNames, customDyeColors, customArmorTrims, customAnimatedDyes };
	}

	private static DataFixer[] getUIAndVisualsDataFixerRules() {
		DataFixer compactorDeletorPreview = (oldFmt, newFmt) -> newFmt.getAsJsonObject("uiAndVisuals").addProperty("compactorDeletorPreview", JsonHelper.getBoolean(oldFmt, "general.compactorDeletorPreview").orElse(true));
		DataFixer dontStripSkinAlphaValues = (oldFmt, newFmt) -> newFmt.getAsJsonObject("uiAndVisuals").addProperty("dontStripSkinAlphaValues", JsonHelper.getBoolean(oldFmt, "general.dontStripSkinAlphaValues").orElse(true));
		DataFixer backpackPreviewWithoutShift = (oldFmt, newFmt) -> newFmt.getAsJsonObject("uiAndVisuals").addProperty("backpackPreviewWithoutShift", JsonHelper.getBoolean(oldFmt, "general.backpackPreviewWithoutShift").orElse(false));
		DataFixer hideEmptyItemTooltips = (oldFmt, newFmt) -> newFmt.getAsJsonObject("uiAndVisuals").add("hideEmptyTooltips", oldFmt.getAsJsonObject("general").get("hideEmptyTooltips"));
		DataFixer fancyCraftingTable = (oldFmt, newFmt) -> newFmt.getAsJsonObject("uiAndVisuals").addProperty("fancyCraftingTable", JsonHelper.getBoolean(oldFmt, "general.fancyCraftingTable").orElse(true));
		DataFixer hideStatusEffectOverlay = (oldFmt, newFmt) -> newFmt.getAsJsonObject("uiAndVisuals").addProperty("hideStatusEffectOverlay", JsonHelper.getBoolean(oldFmt, "general.hideStatusEffectOverlay").orElse(false));
		DataFixer chestValue = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("chestValue")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("chestValue", oldFmt.getAsJsonObject("general").get("chestValue"));
			}
		};
		DataFixer itemCooldown = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("itemCooldown")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("itemCooldown", oldFmt.getAsJsonObject("general").get("itemCooldown"));
			}
		};
		DataFixer titleContainer = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("titleContainer")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("titleContainer", oldFmt.getAsJsonObject("general").get("titleContainer"));
			}
		};
		DataFixer tabHud = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("tabHud")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("tabHud", oldFmt.getAsJsonObject("general").get("tabHud"));
			}
		};
		DataFixer fancyAuctionHouse = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("fancyAuctionHouse")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("fancyAuctionHouse", oldFmt.getAsJsonObject("general").get("fancyAuctionHouse"));
			}
		};
		DataFixer bars = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("bars")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("bars", oldFmt.getAsJsonObject("general").get("bars"));
			}
		};
		DataFixer waypoints = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("waypoints")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("waypoints", oldFmt.getAsJsonObject("general").get("waypoints"));
			}
		};
		DataFixer teleportOverlay = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("teleportOverlay")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("teleportOverlay", oldFmt.getAsJsonObject("general").get("teleportOverlay"));
			}
		};
		DataFixer searchOverlay = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("searchOverlay")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("searchOverlay", oldFmt.getAsJsonObject("general").get("searchOverlay"));
			}
		};

		DataFixer flameOverlay = (oldFmt, newFmt) -> {
			if (oldFmt.getAsJsonObject("general").has("flameOverlay")) {
				newFmt.getAsJsonObject("uiAndVisuals").add("flameOverlay", oldFmt.getAsJsonObject("general").get("flameOverlay"));
			}
		};

		return new DataFixer[] { compactorDeletorPreview, dontStripSkinAlphaValues, backpackPreviewWithoutShift, fancyCraftingTable, hideStatusEffectOverlay, chestValue, itemCooldown, titleContainer,
				tabHud, fancyAuctionHouse, bars, waypoints, teleportOverlay, searchOverlay, flameOverlay, hideEmptyItemTooltips };
	}

	private static DataFixer[] getHelpersDataFixerRules() {
		DataFixer newYearCakesHelper = (oldFmt, newFmt) -> newFmt.getAsJsonObject("helpers").addProperty("enableNewYearCakesHelper", JsonHelper.getBoolean(oldFmt, "general.enableNewYearCakesHelper").orElse(true));
		DataFixer mythologicalRitual = (oldFmt, newFmt) -> newFmt.getAsJsonObject("helpers").add("mythologicalRitual", oldFmt.getAsJsonObject("general").getAsJsonObject("mythologicalRitual"));
		DataFixer experiments = (oldFmt, newFmt) -> newFmt.getAsJsonObject("helpers").add("experiments", oldFmt.getAsJsonObject("general").getAsJsonObject("experiments"));
		DataFixer fishing = (oldFmt, newFmt) -> newFmt.getAsJsonObject("helpers").add("fishing", oldFmt.getAsJsonObject("general").getAsJsonObject("fishing"));
		DataFixer fairySouls = (oldFmt, newFmt) -> newFmt.getAsJsonObject("helpers").add("fairySouls", oldFmt.getAsJsonObject("general").getAsJsonObject("fairySouls"));

		return new DataFixer[] { newYearCakesHelper, mythologicalRitual, experiments, fishing, fairySouls };
	}

	private static DataFixer[] getDungeonsDataFixerRules() {
		DataFixer uncategorized = (oldFmt, newFmt) -> {
			JsonObject dungeonsOld = oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons");
			JsonObject dungeonsNew = newFmt.getAsJsonObject("dungeons");

			dungeonsNew.addProperty("fancyPartyFinder", JsonHelper.getBoolean(oldFmt, "general.betterPartyFinder").orElse(true));
			dungeonsNew.add("croesusHelper", dungeonsOld.get("croesusHelper"));
			dungeonsNew.addProperty("playerSecretsTracker", JsonHelper.getBoolean(dungeonsOld, "playerSecretsTracker").orElse(false));
			dungeonsNew.add("starredMobGlow", dungeonsOld.get("starredMobGlow"));
			dungeonsNew.addProperty("starredMobBoundingBoxes", JsonHelper.getBoolean(dungeonsOld, "starredMobBoundingBoxes").orElse(true));
			dungeonsNew.add("allowDroppingProtectedItems", dungeonsOld.get("allowDroppingProtectedItems"));
		};

		DataFixer map = (oldFmt, newFmt) -> {
			JsonObject dungeonsOld = oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons");
			JsonObject mapConfig = new JsonObject();

			mapConfig.add("enableMap", dungeonsOld.get("enableMap"));
			mapConfig.add("mapScaling", dungeonsOld.get("mapScaling"));
			mapConfig.add("mapX", dungeonsOld.get("mapX"));
			mapConfig.add("mapY", dungeonsOld.get("mapY"));

			newFmt.getAsJsonObject("dungeons").add("dungeonMap", mapConfig);
		};

		DataFixer puzzleSolvers = (oldFmt, newFmt) -> {
			JsonObject dungeonsOld = oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons");
			JsonObject solverConfig = new JsonObject();

			solverConfig.add("solveThreeWeirdos", dungeonsOld.get("solveThreeWeirdos"));
			solverConfig.add("blazeSolver", dungeonsOld.get("blazeSolver"));
			solverConfig.add("creeperSolver", dungeonsOld.get("creeperSolver"));
			solverConfig.add("solveTrivia", dungeonsOld.get("solveTrivia"));
			solverConfig.add("solveTicTacToe", dungeonsOld.get("solveTicTacToe"));
			solverConfig.add("solveWaterboard", dungeonsOld.get("solveWaterboard"));
			solverConfig.add("solveBoulder", dungeonsOld.get("solveBoulder"));
			solverConfig.add("solveIceFill", dungeonsOld.get("solveIceFill"));
			solverConfig.add("solveSilverfish", dungeonsOld.get("solveSilverfish"));

			newFmt.getAsJsonObject("dungeons").add("puzzleSolvers", solverConfig);
		};

		DataFixer professor = (oldFmt, newFmt) -> {
			JsonObject dungeonsOld = oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons");
			JsonObject professorConfig = new JsonObject();

			professorConfig.addProperty("fireFreezeStaffTimer", JsonHelper.getBoolean(dungeonsOld, "fireFreezeStaffTimer").orElse(true));
			professorConfig.addProperty("floor3GuardianHealthDisplay", JsonHelper.getBoolean(dungeonsOld, "floor3GuardianHealthDisplay").orElse(true));

			newFmt.getAsJsonObject("dungeons").add("theProfessor", professorConfig);
		};

		DataFixer livid = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("livid", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("lividColor"));
		DataFixer terminalSolvers = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("terminals", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("terminals"));
		DataFixer secrets = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("secretWaypoints", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("secretWaypoints"));
		DataFixer mimicMessage = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("mimicMessage", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("mimicMessage"));
		DataFixer doorHighlight = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("doorHighlight", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("doorHighlight"));
		DataFixer dungeonScore = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("dungeonScore", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("dungeonScore"));
		DataFixer dungeonChestProfit = (oldFmt, newFmt) -> newFmt.getAsJsonObject("dungeons").add("dungeonChestProfit", oldFmt.getAsJsonObject("locations").getAsJsonObject("dungeons").get("dungeonChestProfit"));

		return new DataFixer[] { uncategorized, map, puzzleSolvers, professor, livid, terminalSolvers, secrets, mimicMessage, doorHighlight, dungeonScore, dungeonChestProfit };
	}

	private static DataFixer[] getCrimsonIsleDataFixerRule() {
		return new DataFixer[] { (oldFmt, newFmt) -> newFmt.add("crimsonIsle", oldFmt.getAsJsonObject("locations").get("crimsonIsle").deepCopy()) };
	}

	private static DataFixer[] getMiningDataFixerRules() {
		DataFixer drillFuel = (oldFmt, newFmt) -> newFmt.getAsJsonObject("mining").addProperty("enableDrillFuel", JsonHelper.getBoolean(oldFmt, "locations.dwarvenMines.enableDrillFuel").orElse(false));
		DataFixer dwarvenMines = (oldFmt, newFmt) -> {
			JsonObject dwarvenOld = oldFmt.getAsJsonObject("locations").getAsJsonObject("dwarvenMines");
			JsonObject dwarvenConfig = new JsonObject();

			dwarvenConfig.add("solveFetchur", dwarvenOld.get("solveFetchur"));
			dwarvenConfig.add("solvePuzzler", dwarvenOld.get("solvePuzzler"));

			newFmt.getAsJsonObject("mining").add("dwarvenMines", dwarvenConfig);
		};

		DataFixer dwarvenHud = (oldFmt, newFmt) -> {
			JsonObject dwarvenHudConfig = new JsonObject();

			dwarvenHudConfig.addProperty("enabledCommissions", JsonHelper.getBoolean(oldFmt, "locations.dwarvenMines.dwarvenHud.enabledCommissions").orElse(false));
			dwarvenHudConfig.addProperty("enabledPowder", JsonHelper.getBoolean(oldFmt, "locations.dwarvenMines.dwarvenHud.enabledPowder").orElse(false));
			dwarvenHudConfig.addProperty("style", JsonHelper.getString(oldFmt, "locations.dwarvenMines.dwarvenHud.style").orElse("SIMPLE"));
			dwarvenHudConfig.addProperty("commissionsX", JsonHelper.getInt(oldFmt, "locations.dwarvenMines.dwarvenHud.x").orElse(10));
			dwarvenHudConfig.addProperty("commissionsY", JsonHelper.getInt(oldFmt, "locations.dwarvenMines.dwarvenHud.y").orElse(10));
			dwarvenHudConfig.addProperty("powderX", JsonHelper.getInt(oldFmt, "locations.dwarvenMines.dwarvenHud.powderX").orElse(10));
			dwarvenHudConfig.addProperty("powderY", JsonHelper.getInt(oldFmt, "locations.dwarvenMines.dwarvenHud.powderY").orElse(70));

			newFmt.getAsJsonObject("mining").add("dwarvenHud", dwarvenHudConfig);
		};
		DataFixer crystalHollowsMap = (oldFmt, newFmt) -> newFmt.getAsJsonObject("mining").add("crystalsHud", oldFmt.getAsJsonObject("locations").getAsJsonObject("dwarvenMines").get("crystalsHud"));
		DataFixer crystalHollowsWaypoints = (oldFmt, newFmt) -> newFmt.getAsJsonObject("mining").add("crystalsWaypoints", oldFmt.getAsJsonObject("locations").getAsJsonObject("dwarvenMines").get("crystalsWaypoints"));
		DataFixer metalDetectorHelper = (oldFmt, newFmt) -> {
			newFmt.getAsJsonObject("mining").add("crystalHollows", new JsonObject()); 
			newFmt.getAsJsonObject("mining").getAsJsonObject("crystalHollows").addProperty("metalDetectorHelper", JsonHelper.getBoolean(oldFmt, "locations.dwarvenMines.metalDetectorHelper").orElse(false));
		};

		return new DataFixer[] { drillFuel, dwarvenMines, dwarvenHud, crystalHollowsMap, crystalHollowsWaypoints, metalDetectorHelper };
	}

	private static DataFixer[] getFarmingDataFixerRules() {
		DataFixer garden = (oldFmt, newFmt) -> newFmt.getAsJsonObject("farming").add("garden", oldFmt.getAsJsonObject("locations").get("garden"));

		return new DataFixer[] { garden };
	}

	private static DataFixer[] getOtherLocationsDataFixerRules() {
		DataFixer barn = (oldFmt, newFmt) -> newFmt.getAsJsonObject("otherLocations").add("barn", oldFmt.getAsJsonObject("locations").get("barn"));
		DataFixer rift = (oldFmt, newFmt) -> newFmt.getAsJsonObject("otherLocations").add("rift", oldFmt.getAsJsonObject("locations").get("rift"));
		DataFixer end = (oldFmt, newFmt) -> newFmt.getAsJsonObject("otherLocations").add("end", oldFmt.getAsJsonObject("locations").get("end"));
		DataFixer spidersDen = (oldFmt, newFmt) -> newFmt.getAsJsonObject("otherLocations").add("spidersDen", oldFmt.getAsJsonObject("locations").get("spidersDen"));

		return new DataFixer[] { barn, rift, end, spidersDen };
	}

	private static DataFixer[] getSlayersDataFixerRules() {
		DataFixer enderman = (oldFmt, newFmt) -> newFmt.getAsJsonObject("slayers").add("endermanSlayer", oldFmt.getAsJsonObject("slayer").get("endermanSlayer"));
		DataFixer vampire = (oldFmt, newFmt) -> newFmt.getAsJsonObject("slayers").add("vampireSlayer", oldFmt.getAsJsonObject("slayer").get("vampireSlayer"));

		return new DataFixer[] { enderman, vampire };
	}

	private static DataFixer[] getChatDataFixerRules() {
		DataFixer mainFixer = (oldFmt, newFmt) -> newFmt.add("chat", oldFmt.get("messages"));

		return new DataFixer[] { mainFixer };
	}


	private static DataFixer[] getQuickNavDataFixerRules() {
		DataFixer toggle = (oldFmt, newFmt) -> newFmt.getAsJsonObject("quickNav").add("enableQuickNav", oldFmt.getAsJsonObject("quickNav").get("enableQuickNav"));
		DataFixer buttonFixer = (oldFmt, newFmt) -> {
			for (int i = 1; i < 13; i++) {
				JsonObject oldButton = oldFmt.getAsJsonObject("quickNav").getAsJsonObject("button" + i);
				JsonObject newButton = new JsonObject();

				newButton.add("render", oldButton.get("render"));
				newButton.add("uiTitle", oldButton.get("uiTitle"));
				newButton.add("clickEvent", oldButton.get("clickEvent"));

				//Item
				JsonObject oldItem = oldButton.getAsJsonObject("item");
				JsonObject newItem = new JsonObject();

				newItem.addProperty("id", oldItem.get("itemName").getAsString());
				newItem.addProperty("count", oldItem.get("count").getAsInt());
				newItem.addProperty("components", nbtToComponents(newItem.get("id").getAsString(), 1, oldItem.get("nbt").getAsString()));

				newButton.add("item", newItem);
				newFmt.getAsJsonObject("quickNav").add("button" + i, newButton);
			}
		};

		return new DataFixer[] { toggle, buttonFixer };
	}

	private static DataFixer[] getMiscDataFixerRules() {
		DataFixer richPresence = (oldFmt, newFmt) -> newFmt.getAsJsonObject("misc").add("richPresence", oldFmt.getAsJsonObject("richPresence"));

		return new DataFixer[] { richPresence };
	}

	private static String nbtToComponents(String id, int count, String nbt) {
		try {
			String nbtString = "{id:\"minecraft:" + id.toLowerCase(Locale.ROOT) + "\",Count:1";
			if (nbt.length() > 2) nbtString += "," + nbt;
			nbtString += "}";

			ItemStack fixed = ItemStackComponentizationFixer.fixUpItem(StringNbtReader.parse(nbtString));

			return ItemStackComponentizationFixer.componentsAsString(fixed);
		} catch (Throwable t) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to convert nbt to components!", t);
		}

		return "[]";
	}

	/**
	 * Represents a data fixer rule.
	 */
	//Could be moved to the data fixer package if multiple classes come to need this
	@FunctionalInterface
	interface DataFixer {
		void apply(JsonObject oldFmt, JsonObject newFmt);
	}
}
