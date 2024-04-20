package de.hysky.skyblocker.skyblock.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EventNotifications {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static long currentTime = System.currentTimeMillis() / 1000;

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(EventNotifications::timeUpdate, 20);

        SkyblockEvents.JOIN.register(EventNotifications::refreshEvents);
    }

    private static final Map<String, LinkedList<SkyblockEvent>> events = new Object2ObjectOpenHashMap<>();

    public static void refreshEvents() {
        CompletableFuture.supplyAsync(() -> {
            try {
                JsonArray jsonElements = SkyblockerMod.GSON.fromJson(Http.sendGetRequest("https://hysky.de/api/calendar"), JsonArray.class);
                return jsonElements.asList().stream().map(JsonElement::getAsJsonObject).toList();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to download warps list", e);
            }
            return List.<JsonObject>of();
        }).thenAccept(eventsList -> {
            events.clear();
            for (JsonObject object : eventsList) {
                if (object.get("timestamp").getAsLong() + object.get("duration").getAsInt() < currentTime) continue;
                SkyblockEvent skyblockEvent = SkyblockEvent.of(object);
                events.computeIfAbsent(object.get("event").getAsString(), s -> new LinkedList<>()).add(skyblockEvent);
            }

            for (Map.Entry<String, LinkedList<SkyblockEvent>> entry : events.entrySet()) {
                entry.getValue().sort(Comparator.comparingLong(SkyblockEvent::start)); // Sort just in case it's not in order for some reason in API
                //LOGGER.info("Next {} is at {}", entry.getKey(), entry.getValue().peekFirst());
            }
        });
    }



    private static void timeUpdate() {
        long newTime = System.currentTimeMillis() / 1000;
        for (Map.Entry<String, LinkedList<SkyblockEvent>> entry : events.entrySet()) {
            LinkedList<SkyblockEvent> nextEvents = entry.getValue();
            SkyblockEvent skyblockEvent = nextEvents.peekFirst();
            if (skyblockEvent == null) continue;
            if (newTime > skyblockEvent.start() + skyblockEvent.duration()) {
                nextEvents.pollFirst();
                skyblockEvent = nextEvents.peekFirst();
                if (skyblockEvent == null) continue;
            }
            if (currentTime + 60 < skyblockEvent.start() && newTime + 60 >= skyblockEvent.start()) {
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                        SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.literal(entry.getKey()),
                        Text.literal("Starting soon!")));
            }
        }
        currentTime = newTime;
    }

    public record SkyblockEvent(long start, int duration, String[] extras, @Nullable String warpCommand) {
        public static SkyblockEvent of(JsonObject jsonObject) {
            String location = jsonObject.get("location").getAsString();
            location = location.isBlank() ? null: location;
            return new SkyblockEvent(jsonObject.get("timestamp").getAsLong(),
                    jsonObject.get("duration").getAsInt(),
                    jsonObject.get("extras").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toArray(String[]::new),
                    location);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("start", start)
                    .append("duration", duration)
                    .append("extras", extras)
                    .append("warpCommand", warpCommand)
                    .toString();
        }
    }
}
