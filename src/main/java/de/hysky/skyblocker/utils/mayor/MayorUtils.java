package de.hysky.skyblocker.utils.mayor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MayorUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(MayorUtils.class);
	private static Mayor mayor = Mayor.EMPTY;
	private static Minister minister = Minister.EMPTY;
	private static List<PerkOverride> mayorPerkOverrides = List.of();
	private static boolean mayorTickScheduled = false;
	private static int mayorTickRetryAttempts = 0;

	private MayorUtils() {}

	/**
	 * Returns the perks that are currently active from the mayor, minister, and any overrides.
	 */
	public static List<String> getActivePerks() {
		Stream<String> mayorPerks = mayor.perks().stream()
				.map(Perk::name);
		Stream<String> ministerPerk = Stream.of(minister.perk().name());
		Stream<String> overriddenMayorPerks = mayorPerkOverrides.stream()
				.filter(PerkOverride::isActive)
				.map(PerkOverride::perk);

		return Stream.concat(Stream.concat(mayorPerks, ministerPerk), overriddenMayorPerks).toList();
	}

	@Init
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
		RenderHelper.runOnRenderThread(() -> {
			// 5 extra minutes to allow the cache to expire. This is a simpler than checking age and subtracting from max age and rescheduling again.
			Scheduler.INSTANCE.schedule(MayorUtils::tickMayorCache, (int) (millisUntilNextMayorChange / 50) + 5 * 60 * 20);
		});
	}

	private static void tickMayorCache() {
		loadMayorPerkOverrides();

		CompletableFuture.supplyAsync(() -> {
			try (Http.ApiResponse response = Http.sendCacheableGetRequest("https://hysky.de/api/skyblock/election", null)) {
				if (!response.ok()) {
					throw new RuntimeException("Received bad http response: " + response.statusCode() + " " + response.content());
				}

				JsonObject json = JsonParser.parseString(response.content()).getAsJsonObject();

				if (!json.get("success").getAsBoolean()) {
					//Can't find a more appropriate exception to throw here.
					throw new RuntimeException("Request failed!");
				}

				JsonObject mayorObject = json.getAsJsonObject("mayor");

				if (mayorObject == null) {
					throw new RuntimeException("No mayor object found in response!");
				}

				return mayorObject;
			} catch (Exception e) {
				throw new RuntimeException(e); //Wrap the exception to be handled by the exceptionally block
			}
		}, Executors.newVirtualThreadPerTaskExecutor()).exceptionally(throwable -> {
			LOGGER.error("[Skyblocker] Failed to get mayor status!", throwable.getCause());
			if (mayorTickRetryAttempts < 5) {
				int minutes = 5 << mayorTickRetryAttempts; //5, 10, 20, 40, 80 minutes
				mayorTickRetryAttempts++;

				LOGGER.warn("[Skyblocker] Retrying in {} minutes.", minutes);
				RenderHelper.runOnRenderThread(() -> {
					Scheduler.INSTANCE.schedule(MayorUtils::tickMayorCache, minutes * 60 * 20);
				});
			} else {
				LOGGER.warn("[Skyblocker] Failed to get mayor status after 5 retries! Stopping further retries until next reboot.");
			}
			return new JsonObject(); //Have to return a value for the thenAccept block.
		}).thenAccept(result -> {
			if (!result.isEmpty()) {
				try {
					mayor = Mayor.CODEC.parse(JsonOps.INSTANCE, result)
							.setPartial(Mayor.EMPTY)
							.resultOrPartial(error -> LOGGER.warn("[Skyblocker] Failed to parse mayor status from the API response. Error: {}", error))
							.get();
				} catch (Exception e) {
					LOGGER.warn("[Skyblocker] Failed to parse mayor status from the API response.", e);
					mayor = Mayor.EMPTY;
				}

				try {
					JsonObject ministerObject = result.getAsJsonObject("minister");

					// Check if ministerObject is not null stops NPE caused by Derpy
					if (ministerObject != null) {
						minister = Minister.CODEC.parse(JsonOps.INSTANCE, ministerObject)
								.setPartial(Minister.EMPTY)
								.resultOrPartial(error -> LOGGER.warn("[Skyblocker] Failed to parse minister status from the API response. Error: {}", error))
								.get();
					} else {
						LOGGER.info("[Skyblocker] No minister data found for the current mayor.");
						minister = Minister.EMPTY;
					}
				} catch (Exception e) {
					LOGGER.warn("[Skyblocker] Failed to parse minister status from the API response.", e);
					minister = Minister.EMPTY;
				}
				LOGGER.info("[Skyblocker] Mayor set to {}, minister set to {}.", mayor, minister);
				scheduleMayorTick(); //Ends up as a cyclic task with finer control over scheduled time
				SkyblockEvents.MAYOR_CHANGE.invoker().onMayorChange();
			}
		});
	}

	private static void loadMayorPerkOverrides() {
		CompletableFuture.runAsync(() -> {
			try {
				String response = Http.sendGetRequest("https://hysky.de/api/mayorperkoverrides");
				mayorPerkOverrides = PerkOverride.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getOrThrow();

				LOGGER.info("[Skyblocker] Loaded {} mayor perk overrides.", mayorPerkOverrides.size());
				SkyblockEvents.MAYOR_CHANGE.invoker().onMayorChange();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to load mayor perk overrides.", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private record PerkOverride(String perk, long from, long to) {
		private static final Codec<PerkOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("perk").forGetter(PerkOverride::perk),
				Codec.LONG.fieldOf("from").forGetter(PerkOverride::from),
				Codec.LONG.fieldOf("to").forGetter(PerkOverride::to)
				).apply(instance, PerkOverride::new));
		private static final Codec<List<PerkOverride>> LIST_CODEC = CODEC.listOf();

		/**
		 * Whether this override is applicable.
		 */
		public boolean isActive() {
			long now = System.currentTimeMillis();

			return now >= this.from && now <= this.to;
		}
	}
}
