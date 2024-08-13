package de.hysky.skyblocker.utils.mayor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MayorUtils {
	private static Mayor mayor = Mayor.EMPTY;
	private static Minister minister = Minister.EMPTY;
	private static boolean mayorTickScheduled = false;
	private static int mayorTickRetryAttempts = 0;
	private static final Logger LOGGER = LoggerFactory.getLogger(MayorUtils.class);

	private MayorUtils() {}

	@NotNull
	public static Mayor getMayor() {
		return mayor;
	}

	@NotNull
	public static Minister getMinister() {
		return minister;
	}

	public static void init() {
		SkyblockEvents.JOIN.register(() -> {
			if (!mayorTickScheduled) {
				tickMayorCache();
				scheduleMayorTick();
				mayorTickScheduled = true;
			}
		});
	}

	private static void scheduleMayorTick() {
		long currentYearMillis = SkyblockTime.getSkyblockMillis() % 446400000L; //446400000ms is 1 year, 105600000ms is the amount of time from early spring 1st to late spring 27th
		// If current time is past late spring 27th, the next mayor change is at next year's spring 27th, otherwise it's at this year's spring 27th
		long millisUntilNextMayorChange = currentYearMillis > 105600000L ? 446400000L - currentYearMillis + 105600000L : 105600000L - currentYearMillis;
		Scheduler.INSTANCE.schedule(MayorUtils::tickMayorCache, (int) (millisUntilNextMayorChange / 50) + 5 * 60 * 20); // 5 extra minutes to allow the cache to expire. This is a simpler than checking age and subtracting from max age and rescheduling again.
	}

	private static void tickMayorCache() {
		CompletableFuture.supplyAsync(() -> {
			try {
				Http.ApiResponse response = Http.sendCacheableGetRequest("https://api.hypixel.net/v2/resources/skyblock/election", null); //Authentication is not required for this endpoint
				if (!response.ok()) throw new HttpResponseException(response.statusCode(), response.content());
				JsonObject json = JsonParser.parseString(response.content()).getAsJsonObject();
				if (!json.get("success").getAsBoolean()) throw new RuntimeException("Request failed!"); //Can't find a more appropriate exception to throw here.
				return json.get("mayor").getAsJsonObject();
			} catch (Exception e) {
				throw new RuntimeException(e); //Wrap the exception to be handled by the exceptionally block
			}
		}).exceptionally(throwable -> {
			LOGGER.error("[Skyblocker] Failed to get mayor status!", throwable.getCause());
			if (mayorTickRetryAttempts < 5) {
				int minutes = 5 << mayorTickRetryAttempts; //5, 10, 20, 40, 80 minutes
				mayorTickRetryAttempts++;
				LOGGER.warn("[Skyblocker] Retrying in {} minutes.", minutes);
				Scheduler.INSTANCE.schedule(MayorUtils::tickMayorCache, minutes * 60 * 20);
			} else {
				LOGGER.warn("[Skyblocker] Failed to get mayor status after 5 retries! Stopping further retries until next reboot.");
			}
			return new JsonObject(); //Have to return a value for the thenAccept block.
		}).thenAccept(result -> {
			if (!result.isEmpty()) {
				mayor = new Mayor(result.get("key").getAsString(),
						result.get("name").getAsString(),
						result.getAsJsonArray("perks")
						      .asList()
						      .stream()
						      .map(JsonElement::getAsJsonObject)
						      .map(object -> new Perk(object.get("name").getAsString(), object.get("description").getAsString()))
						      .toList());
				JsonObject ministerObject = result.getAsJsonObject("minister");
				minister = new Minister(ministerObject.get("key").getAsString(),
						ministerObject.get("name").getAsString(),
						ministerObject.getAsJsonArray("perk")
						      .asList()
						      .stream()
						      .map(JsonElement::getAsJsonObject)
						      .map(object -> new Perk(object.get("name").getAsString(), object.get("description").getAsString()))
						      .findFirst().orElse(Perk.EMPTY));
				LOGGER.info("[Skyblocker] Mayor set to {}, minister set to {}.", mayor.name(), minister.name());
				scheduleMayorTick(); //Ends up as a cyclic task with finer control over scheduled time
			}
		});
	}
}
