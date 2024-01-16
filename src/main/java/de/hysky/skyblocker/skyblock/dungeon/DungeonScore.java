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
	private static final Pattern MIMICLESS_FLOORS_PATTERN = Pattern.compile("[EFM][12345]?");

	private static String currentFloor;
	private static boolean sent270;
	private static boolean sent300;
	private static boolean isMimicKilled;
	private static boolean dungeonStarted;
	private static boolean isMayorPaul;
	private static boolean firstDeathHasSpiritPet;
	private static boolean bloodRoomCompleted;
	private static long startingTime;
	private static int puzzleCount;
	private static int deathCount;
	private static int score;
	private static final Map<String, Boolean> SpiritPetCache = new HashMap<>();

	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(DungeonScore::tick, 20);
		SkyblockEvents.LEAVE.register(SpiritPetCache::clear);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (!Utils.isInDungeons() || !dungeonStarted) return;
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
		if (!dungeonStarted) {
			if (checkIfDungeonStarted()) onDungeonStart();
			return;
		}
		score = calculateScore();
		if (!sent270 && score >= 270 && score < 300) {
			if (SCORE_CONFIG.enableDungeonScore270Message) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270"));
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
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300"));
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
		currentFloor = "";
		sent270 = false;
		sent300 = false;
		isMimicKilled = false;
		dungeonStarted = false;
		isMayorPaul = false;
		firstDeathHasSpiritPet = false;
		bloodRoomCompleted = false;
		startingTime = 0L;
		puzzleCount = 0;
		deathCount = 0;
		score = 0;
	}

	private static void onDungeonStart() {
		setCurrentFloor();
		dungeonStarted = true;
		puzzleCount = getPuzzleCount();
		isMayorPaul = Utils.getMayor().equals("Paul");
		startingTime = System.currentTimeMillis();
	}

	private static int calculateScore() {
		int totalScore = calculateTimeScore() + calculateExploreScore() + calculateSkillScore() + calculateBonusScore();
		if (currentFloor.equals("E")) return (int) (totalScore * 0.7);
		return totalScore;
	}

	private static int calculateSkillScore() {
		return 20 + (int) Math.floor(80.0 * (getCompletedRooms() + getExtraCompletedRooms()) / getTotalRooms()) - getPuzzlePenalty() - getDeathScorePenalty();
	}

	private static int calculateExploreScore() {
		int completedRoomScore = (int) Math.floor(60.0 * (getCompletedRooms() + getExtraCompletedRooms()) / getTotalRooms());
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
		if (timePastRequirement < 20) return score - (int) timePastRequirement / 2;
		if (timePastRequirement < 40) return score - (int) (10 + (timePastRequirement - 20) / 4);
		if (timePastRequirement < 50) return score - (int) (15 + (timePastRequirement - 40) / 5);
		if (timePastRequirement < 60) return score - (int) (17 + (timePastRequirement - 50) / 6);
		return score - (int) (18 + (2.0 / 3.0) + (timePastRequirement - 60) / 7);
	}

	private static int calculateBonusScore() {
		int paulScore = isMayorPaul ? 10 : 0;
		int cryptsScore = Math.min(getCrypts(), 5);
		int mimicScore = isMimicKilled ? 2 : 0;
		if (getSecretsPercentage() >= 100 && !MIMICLESS_FLOORS_PATTERN.matcher(currentFloor).matches()) mimicScore = 2; //If mimic kill is not announced but all secrets are found, mimic must've been killed
		return paulScore + cryptsScore + mimicScore;
	}

	private static boolean checkIfDungeonStarted() {
		return Utils.STRING_SCOREBOARD.stream().noneMatch(s -> DUNGEON_START_PATTERN.matcher(s).matches());
	}

	public static boolean isEntityMimic(Entity entity) {
		if (!Utils.isInDungeons()) return false;
		if (MIMICLESS_FLOORS_PATTERN.matcher(currentFloor).matches()) return false;
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
		if (MIMIC_MESSAGES_CONFIG.sendMimicMessages) MessageScheduler.INSTANCE.sendMessageAfterCooldown(MIMIC_MESSAGES_CONFIG.mimicMessage);
		isMimicKilled = true;
	}

	public static void setMimicKilled(boolean state) {
		isMimicKilled = state;
	}

	//This is not very accurate at the beginning of the dungeon since clear percentage is rounded to the closest integer, so at lower percentages its effect on the result is quite high.
	//For example: If clear percentage is 7% with a single room completed, it can be rounded from 6.5 or 7.49. In that range, the actual total room count can be either 14 or 15 while our result is 14.
	//Score might fluctuate at first if the total room amount calculated changes as it gets more accurate with each room completed.
	private static int getTotalRooms() {
		return (int) Math.round(getCompletedRooms() / getClearPercentage());
	}

	private static int getCompletedRooms() {
		Matcher matcher = PlayerListMgr.regexAt(43, COMPLETED_ROOMS_PATTERN);
		return matcher != null ? Integer.parseInt(matcher.group("rooms")) : 0;
	}

	//This is needed for calculating the score before going in the boss room & completing the blood room, so we have the result sooner
	//It might cause score to fluctuate when completing the blood room or entering the boss room as there's a slight delay between the room being completed (boolean set to true) and the scoreboard updating
	private static int getExtraCompletedRooms() {
		if (!bloodRoomCompleted) return 2;
		if (!DungeonManager.isInBoss()) return 1;
		return 0;
	}

	private static double getClearPercentage() {
		for (String sidebarLine : Utils.STRING_SCOREBOARD) {
			Matcher clearMatcher = CLEARED_PATTERN.matcher(sidebarLine);
			if (!clearMatcher.matches()) continue;
			return Double.parseDouble(clearMatcher.group("cleared")) / 100.0;
		}
		LOGGER.error("[Skyblocker] Clear pattern doesn't match!");
		return 0;
	}

	//Score might fluctuate when the first death has spirit pet as the boolean will be set to true after getting a response from the api, which might take a while
	private static int getDeathScorePenalty() {
		return deathCount * 2 - (firstDeathHasSpiritPet ? 1 : 0);
	}

	private static int getPuzzleCount() {
		Matcher matcher = PlayerListMgr.regexAt(47, PUZZLE_COUNT_PATTERN);
		return matcher != null ? Integer.parseInt(matcher.group("count")) : 0;
	}

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
				LOGGER.error("[Skyblocker] Spirit pet lookup by name failed! Name: {} - Cause: {}", name, e.getMessage());
			}
			return false;
		});
	}

	private static void checkMessageForDeaths(String message) {
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
				.thenAccept(hasSpiritPet -> firstDeathHasSpiritPet = hasSpiritPet);
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
		LOGGER.error("[Skyblocker] Floor pattern doesn't match!");
	}

	public static int getScore() {
		return score;
	}

	public static boolean isDungeonStarted() {
		return dungeonStarted;
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

