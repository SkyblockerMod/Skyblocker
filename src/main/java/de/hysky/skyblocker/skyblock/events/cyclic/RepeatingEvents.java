package de.hysky.skyblocker.skyblock.events.cyclic;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.SkyblockEvent;
import de.hysky.skyblocker.skyblock.events.SkyblockEvents;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class RepeatingEvents {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static List<CyclicEvent> cyclicEvents = List.of();
	private static Multimap<SkyblockEvent, CyclicEvent> nameToEvent = MultimapBuilder.hashKeys().arrayListValues().build();

	@Init
	public static void init() {
		CompletableFuture.supplyAsync(RepeatingEvents::fetchCyclicEvents).thenAcceptAsync(e -> {
			cyclicEvents = e;
			populate();
		}, Minecraft.getInstance());
	}

	private static void populate() {
		nameToEvent = cyclicEvents.stream().collect(() -> Multimaps.newListMultimap(new Reference2ObjectOpenHashMap<>(), () -> new ArrayList<>(1)), (map, event) -> map.put(SkyblockEvents.getOrNew(event.name()), event), Multimap::putAll);
	}

	private static List<CyclicEvent> fetchCyclicEvents() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("event_test.json");
		try (Reader reader = Files.newBufferedReader(path)) {
			DataResult<List<CyclicEvent>> parsed = CyclicEvent.CODEC.listOf().parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
			return parsed.ifError(error -> LOGGER.error("Failed to parse file: {}", error.message())).result().orElse(List.of());
		} catch (Exception e) {
			LOGGER.error("Failed to load event_test.json", e);
			return List.of();
		}
	}

	public static Optional<EventInstance> getNext(SkyblockEvent event, Instant after, boolean includeRunning) {
		Collection<CyclicEvent> events = nameToEvent.get(event);
		if (events.isEmpty()) return Optional.empty();
		EventInstance instance = null;
		for (CyclicEvent cyclicEvent : events) {
			Optional<EventInstance> nextAfter = cyclicEvent.findNextAfter(after, includeRunning);
			if (nextAfter.isPresent() && (instance == null || nextAfter.get().start().isBefore(instance.start()))) {
				instance = nextAfter.get();
			}
		}
		return Optional.ofNullable(instance);
	}
}
