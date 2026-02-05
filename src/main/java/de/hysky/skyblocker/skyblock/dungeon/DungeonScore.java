package de.hysky.skyblocker.skyblock.dungeon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretSync;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.ProfileUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonScore {
	private static final Supplier<DungeonsConfig.DungeonScore> SCORE_CONFIG = () -> SkyblockerConfigManager.get().dungeons.dungeonScore;
	private static final Supplier<DungeonsConfig.MimicMessage> MIMIC_MESSAGE_CONFIG = () -> SkyblockerConfigManager.get().dungeons.mimicMessage;
	private static final Supplier<DungeonsConfig.PrinceMessage> PRINCE_MESSAGE_CONFIG = () -> SkyblockerConfigManager.get().dungeons.princeMessage;
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Dungeon Score");
	//Scoreboard patterns
	private static final Pattern CLEARED_PATTERN = Pattern.compile("Cleared: (?<cleared>\\d+)%.*");
	private static final Pattern FLOOR_PATTERN = Pattern.compile(".*?(?=T)The Catacombs \\((?<floor>[EFM]\\D*\\d*)\\)");
	//Playerlist patterns
	private static final Pattern SECRETS_PATTERN = Pattern.compile("Secrets Found: (?<secper>\\d+\\.?\\d*)%");
	private static final Pattern PUZZLES_PATTERN = Pattern.compile(".+?(?=:): \\[(?<state>.)](?: \\(\\w*\\))?");
	private static final Pattern PUZZLE_COUNT_PATTERN = Pattern.compile("Puzzles: \\((?<count>\\d+)\\)");
	private static final Pattern CRYPTS_PATTERN = Pattern.compile("Crypts: (?<crypts>\\d+)");
	private static final Pattern COMPLETED_ROOMS_PATTERN = Pattern.compile(" *Completed Rooms: (?<rooms>\\d+)");
	//Chat patterns
	private static final Pattern DEATHS_PATTERN = Pattern.compile(" \\u2620 (?<whodied>\\S+) .*");
	//.*?(?:Mimic dead!?|Mimic Killed!|\$SKYTILS-DUNGEON-SCORE-MIMIC\$)$
	private static final Pattern MIMIC_PATTERN = Pattern.compile(".*?(?:Mimic dead!?|Mimic Killed!|\\$SKYTILS-DUNGEON-SCORE-MIMIC\\$)$");
	private static final String PRINCE_KILL_MESSAGE = "A Prince falls. +1 Bonus Score";
	//.*?(?:Prince dead!?|Prince Killed!)$
	private static final Pattern PRINCE_PATTERN = Pattern.compile(".*?(?:Prince dead!?|Prince Killed!)$");
	//Other patterns
	private static final Pattern MIMIC_FLOORS_PATTERN = Pattern.compile("[FM][67]");
	//Score messages sent in party chat
	private static final String MIMIC_MESSAGE = "Mimic dead!";
	private static final String PRINCE_MESSAGE = "Prince dead!";

	private static boolean isMayorPaul = false;

	private static FloorRequirement floorRequirement = FloorRequirement.NONE;
	private static String currentFloor = "";
	private static boolean isCurrentFloorEntrance;
	private static boolean floorHasMimics;
	private static boolean sentCrypts;
	private static boolean sent270;
	private static boolean sent300;
	private static boolean mimicKilled;
	private static boolean princeKilled;
	private static boolean dungeonStarted;
	private static boolean firstDeathHasSpiritPet;
	private static boolean bloodRoomCompleted;
	private static long startingTime;
	private static int puzzleCount;
	private static int deathCount;
	private static int score;

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(DungeonScore::tick, 20);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		DungeonEvents.DUNGEON_STARTED.register(DungeonScore::onDungeonStart);
		DungeonEvents.DUNGEON_ENDED.register(DungeonScore::reset);
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (overlay || !Utils.isInDungeons()) return true;
			String str = message.getString();
			if (dungeonStarted) {
				checkMessageForDeaths(str);
				checkMessageForWatcher(str);
				if (floorHasMimics) checkMessageForMimic(str); //Only called when the message is not cancelled & isn't on the action bar, complementing MimicFilter
				checkMessageForPrince(str);
			}

			return true;
		});
		SkyblockEvents.MAYOR_CHANGE.register(() -> isMayorPaul = MayorUtils.getActivePerks().contains("EZPZ"));
	}

	public static void tick() {
		Minecraft client = Minecraft.getInstance();
		if (!Utils.isInDungeons() || client.player == null) {
			reset();
			return;
		}
		if (!dungeonStarted) return;

		score = calculateScore();
		if (!sent270 && !sent300 && score >= 270 && score < 300) {
			if (SCORE_CONFIG.get().enableDungeonScore270Message) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.get().dungeonScore270Message.replaceAll("\\[score]", "270"), true);
			}
			if (SCORE_CONFIG.get().enableDungeonScore270Title) {
				client.gui.resetTitleTimes();
				client.gui.setTitle(Component.nullToEmpty(SCORE_CONFIG.get().dungeonScore270Message.replaceAll("\\[score]", "270")));
			}
			if (SCORE_CONFIG.get().enableDungeonScore270Sound) {
				client.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 100f, 0.1f);
			}
			sent270 = true;
		}

		int crypts = getCrypts();
		if (!sentCrypts && score >= SCORE_CONFIG.get().dungeonCryptsMessageThreshold && crypts < 5) {
			if (SCORE_CONFIG.get().enableDungeonCryptsMessage) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.get().dungeonCryptsMessage.replaceAll("\\[crypts]", String.valueOf(crypts)), true);
			}
			sentCrypts = true;
		}

		if (!sent300 && score >= 300) {
			if (SCORE_CONFIG.get().enableDungeonScore300Message) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.get().dungeonScore300Message.replaceAll("\\[score]", "300"), true);
			}
			if (SCORE_CONFIG.get().enableDungeonScore300Title) {
				client.gui.resetTitleTimes();
				client.gui.setTitle(Component.nullToEmpty(SCORE_CONFIG.get().dungeonScore300Message.replaceAll("\\[score]", "300")));
			}
			if (SCORE_CONFIG.get().enableDungeonScore300Sound) {
				client.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 100f, 0.1f);
			}
			sent300 = true;
		}
	}

	private static void reset() {
		floorRequirement = FloorRequirement.NONE;
		currentFloor = "";
		isCurrentFloorEntrance = false;
		floorHasMimics = false;
		sentCrypts = false;
		sent270 = false;
		sent300 = false;
		mimicKilled = false;
		princeKilled = false;
		dungeonStarted = false;
		firstDeathHasSpiritPet = false;
		bloodRoomCompleted = false;
		startingTime = 0L;
		puzzleCount = 0;
		deathCount = 0;
		score = 0;
	}

	private static void onDungeonStart() {
		reset();
		setCurrentFloor();
		dungeonStarted = true;
		puzzleCount = getPuzzleCount();
		startingTime = System.currentTimeMillis();
		floorRequirement = FloorRequirement.valueOf(currentFloor);
		floorHasMimics = MIMIC_FLOORS_PATTERN.matcher(currentFloor).matches();
		if (currentFloor.equals("E")) isCurrentFloorEntrance = true;
	}

	private static int calculateScore() {
		if (isCurrentFloorEntrance) return Math.round(calculateTimeScore() * 0.7f) + Math.round(calculateExploreScore() * 0.7f) + Math.round(calculateSkillScore() * 0.7f) + Math.round(calculateBonusScore() * 0.7f);
		return calculateTimeScore() + calculateExploreScore() + calculateSkillScore() + calculateBonusScore();
	}

	private static int calculateSkillScore() {
		int totalRooms = getTotalRooms(); //This is necessary to avoid division by 0 at the start of dungeons, which results in infinite score
		int completedRoomScore = Math.clamp((totalRooms != 0 ? (int) (80.0 * (getCompletedRooms() + getExtraCompletedRooms()) / totalRooms) : 0), 0, 80);
		return 20 + Math.clamp(completedRoomScore - getPuzzlePenalty() - getDeathScorePenalty(), 0, 80);
	}

	private static int calculateExploreScore() {
		int totalRooms = getTotalRooms(); //This is necessary to avoid division by 0 at the start of dungeons, which results in infinite score
		int completedRoomScore = Math.clamp(totalRooms != 0 ? (int) (60.0 * (getCompletedRooms() + getExtraCompletedRooms()) / totalRooms) : 0, 0, 60);
		int secretsScore = Math.clamp((int) (40 * Math.min(floorRequirement.percentage, getSecretsPercentage()) / floorRequirement.percentage), 0, 40);
		return completedRoomScore + secretsScore; //Clamped between 0 and 100 due to the 2 clamps above
	}

	private static int calculateTimeScore() {
		int score = 100;
		int timeSpent = (int) (System.currentTimeMillis() - startingTime) / 1000;
		if (timeSpent < floorRequirement.timeLimit) return score;

		double timePastRequirement = ((double) (timeSpent - floorRequirement.timeLimit) / floorRequirement.timeLimit) * 100;
		if (timePastRequirement < 20) return score - (int) timePastRequirement / 2;
		if (timePastRequirement < 40) return score - (int) (10 + (timePastRequirement - 20) / 4);
		if (timePastRequirement < 50) return score - (int) (15 + (timePastRequirement - 40) / 5);
		if (timePastRequirement < 60) return score - (int) (17 + (timePastRequirement - 50) / 6);
		//This can theoretically go down to -20 if the time limit is one of the lower ones like 480, but individual score categories can't go below 0, hence the clamp
		return Math.clamp(score - (int) (18 + (2.0 / 3.0) + (timePastRequirement - 60) / 7), 0, 100);
	}

	private static int calculateBonusScore() {
		int paulScore = isMayorPaul ? 10 : 0;
		int cryptsScore = Math.clamp(getCrypts(), 0, 5);
		int mimicScore = mimicKilled ? 2 : 0;
		if (getSecretsPercentage() >= 100 && floorHasMimics) mimicScore = 2; //If mimic kill is not announced but all secrets are found, mimic must've been killed
		int princeScore = princeKilled ? 1 : 0;
		return paulScore + cryptsScore + mimicScore + princeScore;
	}

	public static boolean isEntityMimic(Entity entity) {
		if (!Utils.isInDungeons() || !floorHasMimics || !(entity instanceof Zombie zombie) || !zombie.isBaby()) return false;
		try {
			List<ItemStack> armor = ItemUtils.getArmor(zombie);
			return armor.stream().allMatch(ItemStack::isEmpty);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to check if entity is a mimic!", e);
			return false;
		}
	}

	public static void handleEntityDeath(Entity entity) {
		if (mimicKilled) return;
		if (!isEntityMimic(entity)) return;
		if (MIMIC_MESSAGE_CONFIG.get().sendMimicMessage) MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + MIMIC_MESSAGE, true);
		SecretSync.syncMimicKilled();
		mimicKilled = true;
	}

	public static void onMimicKill() {
		mimicKilled = true;
	}

	public static void onPrinceKill(boolean fromHypixel) {
		if (princeKilled) return;
		//Ensure that we don't send a prince kill message if a teammate does
		if (fromHypixel) {
			if (PRINCE_MESSAGE_CONFIG.get().sendPrinceMessage) MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + PRINCE_MESSAGE, true);
			SecretSync.syncPrinceKilled();
		}
		princeKilled = true;
	}

	public static boolean wasPrinceKilled() {
		return princeKilled;
	}

	//This is not very accurate at the beginning of the dungeon since clear percentage is rounded to the closest integer, so at lower percentages its effect on the result is quite high.
	//For example: If clear percentage is 7% with a single room completed, it can be rounded from 6.5 or 7.49. In that range, the actual total room count can be either 14 or 15 while our result is 14.
	//Score might fluctuate at first if the total room amount calculated changes as it gets more accurate with each room completed.
	private static int getTotalRooms() {
		return (int) Math.round(getCompletedRooms() / getClearPercentage());
	}

	private static int getCompletedRooms() {
		Matcher matcher = PlayerListManager.regexAt(43, COMPLETED_ROOMS_PATTERN);
		return matcher != null ? Integer.parseInt(matcher.group("rooms")) : 0;
	}

	//This is needed for calculating the score before going in the boss room & completing the blood room, so we have the result sooner
	//It might cause score to fluctuate when completing the blood room or entering the boss room as there's a slight delay between the room being completed (boolean set to true) and the scoreboard updating
	private static int getExtraCompletedRooms() {
		if (!bloodRoomCompleted) return isCurrentFloorEntrance ? 1 : 2;
		if (!DungeonManager.isInBoss() && !isCurrentFloorEntrance) return 1;
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
		Matcher matcher = PlayerListManager.regexAt(47, PUZZLE_COUNT_PATTERN);
		return matcher != null ? Integer.parseInt(matcher.group("count")) : 0;
	}

	private static int getPuzzlePenalty() {
		int incompletePuzzles = 0;
		for (int index = 0; index < puzzleCount; index++) {
			Matcher puzzleMatcher = PlayerListManager.regexAt(48 + index, PUZZLES_PATTERN);
			if (puzzleMatcher == null) break;
			if (puzzleMatcher.group("state").matches("[✖✦]")) incompletePuzzles++;
		}
		return incompletePuzzles * 10;
	}

	private static double getSecretsPercentage() {
		Matcher matcher = PlayerListManager.regexAt(44, SECRETS_PATTERN);
		return matcher != null ? Double.parseDouble(matcher.group("secper")) : 0;
	}

	private static int getCrypts() {
		Matcher matcher = PlayerListManager.regexAt(33, CRYPTS_PATTERN);
		if (matcher == null) matcher = PlayerListManager.regexAt(32, CRYPTS_PATTERN); //If class milestone 9 is reached, crypts goes up by 1
		return matcher != null ? Integer.parseInt(matcher.group("crypts")) : 0;
	}

	private static boolean hasSpiritPet(@Nullable JsonObject player, String name) {
		if (player == null) {
			LOGGER.error("[Skyblocker] Spirit pet lookup by name failed! (likely due to an earlier error!) Name: {}", name);
			return false;
		}

		try {
			for (JsonElement pet : player.getAsJsonObject("pets_data").getAsJsonArray("pets")) {
				if (!pet.getAsJsonObject().get("type").getAsString().equals("SPIRIT")) continue;
				if (!pet.getAsJsonObject().get("tier").getAsString().equals("LEGENDARY")) continue;

				return true;
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Spirit pet lookup by name failed! Name: {}", name, e);
		}
		return false;
	}

	private static void checkMessageForDeaths(String message) {
		//noinspection UnnecessaryUnicodeEscape
		if (!message.startsWith("\u2620", 1)) return;
		Matcher matcher = DEATHS_PATTERN.matcher(message);
		if (!matcher.matches()) return;
		deathCount++;
		if (deathCount > 1) return;
		final String whoDied = matcher.group("whodied").transform(s -> {
			if (s.equals("You")) return Minecraft.getInstance().getUser().getName(); //This will be wrong if the dead player is called 'You' but that's unlikely
			else return s;
		});
		ProfileUtils.fetchProfileMember(whoDied).thenAccept(player -> firstDeathHasSpiritPet = hasSpiritPet(player, whoDied));
	}

	private static void checkMessageForWatcher(String message) {
		if (message.equals("[BOSS] The Watcher: You have proven yourself. You may pass.")) {
			Scheduler.INSTANCE.schedule(() -> {
				bloodRoomCompleted = true;
				tick();
			}, 20 * 5 + 10);
		}
	}

	private static void checkMessageForMimic(String message) {
		if (!MIMIC_PATTERN.matcher(message).matches()) return;
		onMimicKill();
	}

	private static void checkMessageForPrince(String message) {
		if (!PRINCE_PATTERN.matcher(message).matches() && !message.equals(PRINCE_KILL_MESSAGE)) return;
		onPrinceKill(message.equals(PRINCE_KILL_MESSAGE));
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

	//Feel free to refactor this if you can think of a better name.
	public static boolean isMimicOnCurrentFloor() {
		return floorHasMimics;
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
		M7(100, 840),
		NONE(0, 0);

		private final int percentage;
		private final int timeLimit;

		FloorRequirement(int percentage, int timeLimit) {
			this.percentage = percentage;
			this.timeLimit = timeLimit;
		}
	}
}
