package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Http.ApiResponse;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

/**
 * Tracks the amount of secrets players get every run
 */
public class SecretsTracker {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecretsTracker.class);

	private static volatile @Nullable TrackedRun currentRun = null;
	private static volatile @Nullable TrackedRun lastRun = null;
	private static volatile long lastRunEnded = 0L;

	@Init
	public static void init() {
		DungeonEvents.DUNGEON_STARTED.register(() -> calculate(RunPhase.START));
		DungeonEvents.DUNGEON_ENDED.register(() -> calculate(RunPhase.END));
	}

	private static void calculate(RunPhase phase) {
		if (!SkyblockerConfigManager.get().dungeons.playerSecretsTracker) return;
		switch (phase) {
			case START -> CompletableFuture.runAsync(() -> {
				TrackedRun newlyStartedRun = new TrackedRun();

				//Initialize players in new run
				for (int i = 0; i < 5; i++) {
					String playerName = getPlayerNameAt(i + 1);

					//The player name will be blank if there isn't a player at that index
					if (!playerName.isEmpty()) {

						//If the player was a part of the last run, had non-empty secret data and that run ended less than 5 mins ago then copy the secret data over
						TrackedRun previousRun = lastRun;
						if (previousRun != null && System.currentTimeMillis() <= lastRunEnded + 300_000 && previousRun.playersSecretData().getOrDefault(playerName, SecretData.EMPTY) != SecretData.EMPTY) {
							newlyStartedRun.playersSecretData().put(playerName, previousRun.playersSecretData().get(playerName));
						} else {
							newlyStartedRun.playersSecretData().put(playerName, getPlayerSecrets(playerName));
						}
					}
				}

				currentRun = newlyStartedRun;
			});

			case END -> CompletableFuture.runAsync(() -> {
				TrackedRun thisRun = currentRun;
				if (thisRun != null) {
					Object2ObjectOpenHashMap<String, SecretData> secretsFound = new Object2ObjectOpenHashMap<>();

					//Update secret counts
					for (Entry<String, SecretData> entry : thisRun.playersSecretData().entrySet()) {
						String playerName = entry.getKey();
						SecretData startingSecrets = entry.getValue();
						SecretData secretsNow = getPlayerSecrets(playerName);
						int secretsPlayerFound = secretsNow.secrets() - startingSecrets.secrets();

						//Add an entry to the secretsFound map with the data - if the secret data from now or the start was cached a warning will be shown
						secretsFound.put(playerName, secretsNow.updated(secretsPlayerFound, startingSecrets.cached() || secretsNow.cached()));
						entry.setValue(secretsNow);
					}

					//Print the results all in one go, so its clean and less of a chance of it being broken up
					for (Map.Entry<String, SecretData> entry : secretsFound.entrySet()) {
						RenderHelper.runOnRenderThread(() -> sendResultMessage(entry.getKey(), entry.getValue()));
					}

					//Swap the current and last run as well as mark the run end time
					lastRunEnded = System.currentTimeMillis();
					lastRun = thisRun;
					currentRun = null;
				} else {
					RenderHelper.runOnRenderThread(SecretsTracker::sendFailureMessage);
				}
			});
		}
	}

	private static void sendResultMessage(String player, SecretData secretData) {
		PlayerEntity playerEntity = MinecraftClient.getInstance().player;
		if (playerEntity == null) return;
		playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secretsTracker.feedback", Text.literal(player).append(" (" + DungeonPlayerManager.getClassFromPlayer(player).displayName() + ")").withColor(0xF57542), "§7" + secretData.secrets(), getCacheText(secretData.cached(), secretData.cacheAge()))), false);
	}

	private static void sendFailureMessage() {
		PlayerEntity playerEntity = MinecraftClient.getInstance().player;
		if (playerEntity == null) return;
		playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secretsTracker.failFeedback")), false);
	}

	private static Text getCacheText(boolean cached, int cacheAge) {
		//noinspection UnnecessaryUnicodeEscape
		return Text.literal("\u2139").styled(style -> style.withColor(cached ? 0xEAC864 : 0x218BFF).withHoverEvent(
				new HoverEvent.ShowText(cached ? Text.translatable("skyblocker.api.cache.HIT", cacheAge) : Text.translatable("skyblocker.api.cache.MISS"))));
	}

	private static String getPlayerNameAt(int index) {
		Matcher matcher = DungeonPlayerManager.getPlayerFromTab(index);

		return matcher != null ? matcher.group("name") : "";
	}

	private static SecretData getPlayerSecrets(String name) {
		String uuid = ApiUtils.name2Uuid(name);

		if (!uuid.isEmpty()) {
			try (ApiResponse response = Http.sendHypixelRequest("player", "?uuid=" + uuid)) {
				return new SecretData(getSecretCountFromAchievements(JsonParser.parseString(response.content()).getAsJsonObject()), response.cached(), response.age());
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Encountered an error while trying to fetch {} secret count!", name + "'s", e);
			}
		}

		return SecretData.EMPTY;
	}

	/**
	 * Gets a player's secret count from their hypixel achievements
	 */
	private static int getSecretCountFromAchievements(JsonObject playerJson) {
		JsonObject player = playerJson.getAsJsonObject("player");
		JsonObject achievements = player.has("achievements") ? player.getAsJsonObject("achievements") : null;
		return (achievements != null && achievements.has("skyblock_treasure_hunter")) ? achievements.get("skyblock_treasure_hunter").getAsInt() : 0;
	}

	/**
	 * This will either reflect the value at the start or the end depending on when this is called
	 */
	private record TrackedRun(Object2ObjectOpenHashMap<String, SecretData> playersSecretData) {
		private TrackedRun() {
			this(new Object2ObjectOpenHashMap<>());
		}
	}

	private record SecretData(int secrets, boolean cached, int cacheAge) {
		private static final SecretData EMPTY = new SecretData(0, false, 0);

		//If only we had Derived Record Creation :( - https://bugs.openjdk.org/browse/JDK-8321133
		private SecretData updated(int secrets, boolean cached) {
			return new SecretData(secrets, cached, this.cacheAge);
		}
	}

	private enum RunPhase {
		START, END
	}
}
