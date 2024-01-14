package de.hysky.skyblocker.skyblock.dungeon;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class DungeonScore {
	private static final SkyblockerConfig.DungeonScore SCORE_CONFIG = SkyblockerConfigManager.get().locations.dungeons.dungeonScore;
	private static final SkyblockerConfig.MimicMessages MIMIC_MESSAGES_CONFIG = SkyblockerConfigManager.get().locations.dungeons.mimicMessages;
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Dungeon Score");
	//Scoreboard patterns
	private static final Pattern CLEARED_PATTERN = Pattern.compile("Cleared: (?<cleared>\\d+)%.*");
	private static final Pattern DUNGEON_START_PATTERN = Pattern.compile("(?:Auto-closing|Starting) in: \\d:\\d+");
	private static final Pattern FLOOR_PATTERN = Pattern.compile(".*?(?=T)The Catacombs \\((?<floor>[EFM]\\D*\\d*)\\)");
	//Playerlist patterns
	private static final Pattern SECRETS_PATTERN = Pattern.compile("Secrets Found: (?<secper>\\d+\\.?\\d*)%");
	private static final Pattern PUZZLES_PATTERN = Pattern.compile(".+?(?=:): \\[(?<state>.)](?: \\(\\w+\\))?");
	private static final Pattern PUZZLE_COUNT_PATTERN = Pattern.compile("Puzzles: \\((?<count>\\d+)\\)");
	private static final Pattern CRYPTS_PATTERN = Pattern.compile("Crypts: (?<crypts>\\d+)");
	private static final Pattern COMPLETED_ROOMS_PATTERN = Pattern.compile(" *Completed Rooms: (?<rooms>\\d+)");
	//Chat patterns
	private static final Pattern DEATHS_PATTERN = Pattern.compile(".*?\u2620 (?<whodied>\\S+) .*");
	//Other patterns
	private static final Pattern MIMIC_FLOOR_FILTER_PATTERN = Pattern.compile("[EFM][12345]?");

	private static String currentFloor;
	private static boolean sent270;
	private static boolean sent300;
	private static boolean isMimicKilled;
	private static int puzzleCount;
	private static boolean isDungeonStarted;
	private static boolean isMayorPaul;
	private static long startingTime;
	private static int deathCount;
	private static boolean firstDeathHasSpiritPet;
	private static boolean bloodRoomCompleted;
	private static final Map<String, Boolean> SpiritPetCache = new HashMap<>();

	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(DungeonScore::tick, 20);
		SkyblockEvents.LEAVE.register(SpiritPetCache::clear);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			String str = message.getString();
			checkMessageForDeaths(str);
			checkMessageForWatcher(str);
		});
	}

	public static void tick() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!Utils.isInDungeons() || client.player == null) {
			reset();
			return;
		}
		if (!isDungeonStarted) {
			if (checkIfDungeonStarted()) onDungeonStart();
			return;
		}
		int score = calculateScore();
		if (!sent270 && score >= 270 && score < 300) {
			if (SCORE_CONFIG.enableDungeonScore270Message) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown(SCORE_CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270"));
			}
			if (SCORE_CONFIG.enableDungeonScore270Title) {
				client.inGameHud.setDefaultTitleFade();
				client.inGameHud.setTitle(Constants.PREFIX.get().append(SCORE_CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270")));
			}
			if (SCORE_CONFIG.enableDungeonScore270Sound) {
				client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f);
			}
			sent270 = true;
		}
		if (!sent300 && score >= 300) {
			if (SCORE_CONFIG.enableDungeonScore300Message) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown(SCORE_CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300"));
			}
			if (SCORE_CONFIG.enableDungeonScore300Title) {
				client.inGameHud.setDefaultTitleFade();
				client.inGameHud.setTitle(Constants.PREFIX.get().append(SCORE_CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300")));
			}
			if (SCORE_CONFIG.enableDungeonScore300Sound) {
				client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f);
			}
			sent300 = true;
		}
	}

	private static void reset() {
		sent270 = false;
		sent300 = false;
		isDungeonStarted = false;
		isMimicKilled = false;
		isMayorPaul = false;
		firstDeathHasSpiritPet = false;
		deathCount = 0;
		currentFloor = "";
	}

	private static void onDungeonStart() {
		setCurrentFloor();
		isDungeonStarted = true;
		puzzleCount = getPuzzleCount();
		isMayorPaul = Utils.getMayor().equals("Paul");
		startingTime = System.currentTimeMillis();
	}

	private static int calculateScore() {
		int timeScore = calculateTimeScore();
		int exploreScore = calculateExploreScore();
		int skillScore = calculateSkillScore();
		int bonusScore = calculateBonusScore();
		int totalScore = timeScore + exploreScore + skillScore + bonusScore;
		if (currentFloor.equals("E")) totalScore = (int) (totalScore * 0.7);
		//Will be this way until ready for pr, so it's easy to debug.
		LOGGER.info("Total Score: {} (Time: {}, Explore: {}, Skill: {}, Bonus: {})", totalScore, timeScore, exploreScore, skillScore, bonusScore);
		return totalScore;
	}

	private static int calculateSkillScore() {
		int extraCompletedRooms = 0; //This is needed for calculating the score before going in, so we have the result sooner
		if (!DungeonManager.isInBoss()) extraCompletedRooms = bloodRoomCompleted ? 1 : 2;
		return 20 + (int) Math.floor(80.0 * (getCompletedRooms() + extraCompletedRooms) / getTotalRooms()) - getPuzzlePenalty() - getDeathScorePenalty();
	}

	private static int calculateExploreScore() {
		int extraCompletedRooms = 0;
		if (!DungeonManager.isInBoss()) extraCompletedRooms = bloodRoomCompleted ? 1 : 2;
		int completedRoomScore = (int) Math.floor(60.0 * (getCompletedRooms() + extraCompletedRooms) / getTotalRooms());
		int percentageRequirement = FloorRequirement.valueOf(currentFloor).percentage;
		int secretsScore = (int) Math.floor(40 * Math.min(percentageRequirement, getSecretsPercentage()) / percentageRequirement);
		return completedRoomScore + secretsScore;
	}

	private static int calculateTimeScore() {
		int score = 100;
		int timeSpent = (int) (System.currentTimeMillis() - startingTime) / 1000;
		int timeRequirement = FloorRequirement.valueOf(currentFloor).timeLimit;
		if (timeSpent < timeRequirement) return score;

		double timePastRequirement = ((double) (timeSpent - timeRequirement) / timeRequirement) * 100;
		if (timePastRequirement >= 0 && timePastRequirement < 20) {
			score -= (int) timePastRequirement / 2;
		} else if (timePastRequirement >= 20 && timePastRequirement < 40) {
			score -= (int) (10 + (timePastRequirement - 20) / 4);
		} else if (timePastRequirement >= 40 && timePastRequirement < 50) {
			score -= (int) (15 + (timePastRequirement - 40) / 5);
		} else if (timePastRequirement >= 50 && timePastRequirement < 60) {
			score -= (int) (17 + (timePastRequirement - 50) / 6);
		} else if (timePastRequirement >= 60) {
			score -= (int) (18 + (2.0 / 3.0) + (timePastRequirement - 60) / 7);
		}
		return score;
	}

	private static int calculateBonusScore() {
		int paulScore = isMayorPaul ? 10 : 0;
		int cryptsScore = Math.min(getCrypts(), 5);
		int mimicScore = isMimicKilled ? 2 : 0;
		if (getSecretsPercentage() >= 100 && !MIMIC_FLOOR_FILTER_PATTERN.matcher(currentFloor).matches()) mimicScore = 2; //If mimic kill is not announced but all secrets are found, mimic must've been killed
		return paulScore + cryptsScore + mimicScore;
	}

	private static boolean checkIfDungeonStarted() {
		return Utils.STRING_SCOREBOARD.stream().anyMatch(s -> DUNGEON_START_PATTERN.matcher(s).matches());
	}

	public static boolean isEntityMimic(Entity entity) {
		if (!Utils.isInDungeons()) return false;
		if (MIMIC_FLOOR_FILTER_PATTERN.matcher(currentFloor).matches()) return false;
		if (entity == null) return false;
		if (!(entity instanceof ZombieEntity zombie)) return false;
		if (!zombie.isBaby()) return false;
		try {
			DefaultedList<ItemStack> armor = (DefaultedList<ItemStack>) zombie.getArmorItems();
			return armor.stream().allMatch(ItemStack::isEmpty);
		} catch (NullPointerException e) {
			return false;
		} catch (ClassCastException f) {
			f.printStackTrace();
			return false;
		}
	}

	public static void handleEntityDeath(Entity entity) {
		if (isMimicKilled) return;
		if (!isEntityMimic(entity)) return;
		isMimicKilled = true;
	}

	public static void setMimicKilled(boolean state) {
		isMimicKilled = state;
	}

	private static int getTotalRooms() {
		int completedRooms = getCompletedRooms();
		return (int) Math.round(completedRooms / getClearPercentage());
	}

	private static int getCompletedRooms() {
		Matcher matcher = PlayerListMgr.regexAt(43, COMPLETED_ROOMS_PATTERN);
		return matcher != null ? Integer.parseInt(matcher.group("rooms")) : 0;
	}

	private static double getClearPercentage() {
		for (String sidebarLine : Utils.STRING_SCOREBOARD) {
			Matcher clearMatcher = CLEARED_PATTERN.matcher(sidebarLine);
			if (!clearMatcher.matches()) continue;
			return Double.parseDouble(clearMatcher.group("cleared")) / 100.0;
		}
		LOGGER.error("Clear pattern doesn't match");
		return 0;
	}

	private static int getDeathScorePenalty() {
		return deathCount * 2 - (firstDeathHasSpiritPet ? 1 : 0);
	}

	private static int getPuzzleCount() {
		Matcher matcher = PlayerListMgr.regexAt(47, PUZZLE_COUNT_PATTERN);
		return matcher != null ? Integer.parseInt(matcher.group("count")) : 0;
	}

	//Possible states: ✖, ✦, ✔
	private static int getPuzzlePenalty() {
		int incompletePuzzles = 0;
		for (int index = 0; index < puzzleCount; index++) {
			Matcher puzzleMatcher = PlayerListMgr.regexAt(48 + index, PUZZLES_PATTERN);
			if (puzzleMatcher == null) break;
			if (puzzleMatcher.group("state").matches("[✖✦]")) incompletePuzzles++;
		}
		return incompletePuzzles * 10;
	}

	private static double getSecretsPercentage() {
		Matcher matcher = PlayerListMgr.regexAt(44, SECRETS_PATTERN);
		return matcher != null ? Double.parseDouble(matcher.group("secper")) : 0;
	}

	private static int getCrypts() {
		Matcher matcher = PlayerListMgr.regexAt(33, CRYPTS_PATTERN);
		if (matcher == null) matcher = PlayerListMgr.regexAt(32, CRYPTS_PATTERN); //If class milestone 9 is reached, crypts goes up by 1
		return matcher != null ? Integer.parseInt(matcher.group("crypts")) : 0;
	}

	public static boolean hasSpiritPet(String name) {
		return SpiritPetCache.computeIfAbsent(name, k -> {
			String playeruuid = ApiUtils.name2Uuid(name);
			try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + playeruuid)) {
				if (!response.ok()) throw new IllegalStateException("Failed to get profile uuid for player " + name + "! Response: " + response.content());
				JsonObject responseJson = JsonParser.parseString(response.content()).getAsJsonObject();

				JsonObject player = StreamSupport.stream(responseJson.getAsJsonArray("profiles").spliterator(), false)
						.map(JsonElement::getAsJsonObject)
						.filter(profile -> profile.getAsJsonPrimitive("selected").getAsBoolean())
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("No selected profile found!?"))
						.getAsJsonObject("members").entrySet().stream()
						.filter(entry -> entry.getKey().equals(playeruuid))
						.map(Map.Entry::getValue)
						.map(JsonElement::getAsJsonObject)
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("Player somehow not found inside their own profile!"));

				for (JsonElement element : player.getAsJsonObject("pets_data").getAsJsonArray("pets")) {
					if (!element.getAsJsonObject().get("type").getAsString().equals("SPIRIT")) continue;
					if (!element.getAsJsonObject().get("tier").getAsString().equals("LEGENDARY")) continue;

					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("[Skyblocker] Spirit pet lookup by name failed! Name: {} - Cause: {}", name, e.getMessage());
			}
			return false;
		});
	}

	private static void checkMessageForDeaths(String message) {
		if (!Utils.isInDungeons()) return;
		if (!message.startsWith("\u2620", 1)) return;
		Matcher matcher = DEATHS_PATTERN.matcher(message);
		if (!matcher.matches()) return;
		deathCount++;
		if (deathCount > 1) return;
		final String whoDied = matcher.group("whodied").transform(s -> {
			if (s.equals("You")) return MinecraftClient.getInstance().player.getName().getString(); //This will be wrong if the dead player is called 'You' but that's unlikely
			else return s;
		});
		CompletableFuture.supplyAsync(() -> hasSpiritPet(whoDied))
				.thenAccept(hasSpiritPet -> {
					firstDeathHasSpiritPet = hasSpiritPet;
				});
	}

	private static void checkMessageForWatcher(String message) {
		if (message.equals("[BOSS] The Watcher: You have proven yourself. You may pass.")) bloodRoomCompleted = true;
	}

	public static void setCurrentFloor() {
		for (String sidebarLine : Utils.STRING_SCOREBOARD) {
			Matcher floorMatcher = FLOOR_PATTERN.matcher(sidebarLine);
			if (!floorMatcher.matches()) continue;
			currentFloor = floorMatcher.group("floor");
			return;
		}
		LOGGER.error("Floor pattern doesn't match");
	}

	enum FloorRequirement {
		E(30, 1200),
		F1(30, 600),
		F2(40, 600),
		F3(50, 600),
		F4(60, 720),
		F5(70, 600),
		F6(85, 720),
		F7(100, 840),
		M1(100, 480),
		M2(100, 480),
		M3(100, 480),
		M4(100, 480),
		M5(100, 480),
		M6(100, 600),
		M7(100, 840);

		private final int percentage;
		private final int timeLimit;

		FloorRequirement(int percentage, int timeLimit) {
			this.percentage = percentage;
			this.timeLimit = timeLimit;
		}
	}
}

