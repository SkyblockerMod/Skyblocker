package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Http.ApiResponse;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks the amount of secrets players get every run
 */
public class SecretsTracker {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecretsTracker.class);
	private static final Pattern TEAM_SCORE_PATTERN = Pattern.compile(" +Team Score: [0-9]+ \\([A-z+]+\\)");

	private static volatile TrackedRun currentRun = null;
	private static volatile TrackedRun lastRun = null;
	private static volatile long lastRunEnded = 0L;

	public static void init() {
		ClientReceiveMessageEvents.GAME.register(SecretsTracker::onMessage);
	}

	//If -1 is somehow encountered, it would be very rare, so I just disregard its possibility for now
	//people would probably recognize if it was inaccurate so yeah
	private static void calculate(RunPhase phase) {
		switch (phase) {
			case START -> CompletableFuture.runAsync(() -> {
				TrackedRun newlyStartedRun = new TrackedRun();

				//Initialize players in new run
				for (int i = 0; i < 5; i++) {
					String playerName = getPlayerNameAt(i + 1);

					//The player name will be blank if there isn't a player at that index
					if (!playerName.isEmpty()) {

						//If the player was a part of the last run (and didn't have -1 secret count) and that run ended less than 5 mins ago then copy the secrets over
						if (lastRun != null && System.currentTimeMillis() <= lastRunEnded + 300_000 && lastRun.secretCounts().getOrDefault(playerName, -1) != -1) {
							newlyStartedRun.secretCounts().put(playerName, lastRun.secretCounts().getInt(playerName));
						} else {
							newlyStartedRun.secretCounts().put(playerName, getPlayerSecrets(playerName).leftInt());
						}
					}
				}

				currentRun = newlyStartedRun;
			});

			case END -> CompletableFuture.runAsync(() -> {
				//In case the game crashes from something
				if (currentRun != null) {
					Object2ObjectOpenHashMap<String, IntIntPair> secretsFound = new Object2ObjectOpenHashMap<>();

					//Update secret counts
					for (Entry<String> entry : currentRun.secretCounts().object2IntEntrySet()) {
						String playerName = entry.getKey();
						int startingSecrets = entry.getIntValue();
						IntIntPair secretsNow = getPlayerSecrets(playerName);
						int secretsPlayerFound = secretsNow.leftInt() - startingSecrets;

						secretsFound.put(playerName, IntIntPair.of(secretsPlayerFound, secretsNow.rightInt()));
						entry.setValue(secretsNow.leftInt());
					}

					//Print the results all in one go, so its clean and less of a chance of it being broken up
					for (Map.Entry<String, IntIntPair> entry : secretsFound.entrySet()) {
						sendResultMessage(entry.getKey(), entry.getValue().leftInt(), entry.getValue().rightInt(), true);
					}

					//Swap the current and last run as well as mark the run end time
					lastRunEnded = System.currentTimeMillis();
					lastRun = currentRun;
					currentRun = null;
				} else {
					sendResultMessage(null, -1, -1, false);
				}
			});
		}
	}

	private static void sendResultMessage(String player, int secrets, int cacheAge, boolean success) {
		PlayerEntity playerEntity = MinecraftClient.getInstance().player;
		if (playerEntity != null) {
			if (success) {
				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secretsTracker.feedback", Text.literal(player).styled(Constants.WITH_COLOR.apply(0xf57542)), "§7" + secrets, getCacheText(cacheAge))));
			} else {
				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secretsTracker.failFeedback")));
			}
		}
	}

	private static Text getCacheText(int cacheAge) {
		return Text.literal("\u2139").styled(style -> style.withColor(cacheAge == -1 ? 0x218bff : 0xeac864).withHoverEvent(
				new HoverEvent(HoverEvent.Action.SHOW_TEXT, cacheAge == -1 ? Text.translatable("skyblocker.api.cache.MISS") : Text.translatable("skyblocker.api.cache.HIT", cacheAge))));
	}

	private static void onMessage(Text text, boolean overlay) {
		if (Utils.isInDungeons() && SkyblockerConfigManager.get().locations.dungeons.playerSecretsTracker) {
			String message = Formatting.strip(text.getString());

			try {
				if (message.equals("[NPC] Mort: Here, I found this map when I first entered the dungeon.")) calculate(RunPhase.START);
				if (TEAM_SCORE_PATTERN.matcher(message).matches()) calculate(RunPhase.END);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Encountered an unknown error while trying to track player secrets!", e);
			}
		}
	}

	private static String getPlayerNameAt(int index) {
		Matcher matcher = PlayerListMgr.regexAt(1 + (index - 1) * 4, DungeonPlayerWidget.PLAYER_PATTERN);

		return matcher != null ? matcher.group("name") : "";
	}

	private static IntIntPair getPlayerSecrets(String name) {
		String uuid = ApiUtils.name2Uuid(name);

		if (!uuid.isEmpty()) {
			try (ApiResponse response = Http.sendHypixelRequest("player", "?uuid=" + uuid)) {
				return IntIntPair.of(getSecretCountFromAchievements(JsonParser.parseString(response.content()).getAsJsonObject()), response.age());
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Encountered an error while trying to fetch {} secret count!", name + "'s", e);
			}
		}

		return IntIntPair.of(-1, -1);
	}

	/**
	 * Gets a player's secret count from their hypixel achievements
	 */
	private static int getSecretCountFromAchievements(JsonObject playerJson) {
		JsonObject player = playerJson.get("player").getAsJsonObject();
		JsonObject achievements = (player.has("achievements")) ? player.get("achievements").getAsJsonObject() : null;
		return (achievements != null && achievements.has("skyblock_treasure_hunter")) ? achievements.get("skyblock_treasure_hunter").getAsInt() : 0;
	}

	/**
	 * This will either reflect the value at the start or the end depending on when this is called
	 */
	private record TrackedRun(Object2IntOpenHashMap<String> secretCounts) {
		private TrackedRun() {
			this(new Object2IntOpenHashMap<>());
		}
	}

	private enum RunPhase {
        START, END
	}
}
