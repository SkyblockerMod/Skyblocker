package de.hysky.skyblocker.skyblock.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EventNotifications {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static long currentTime = System.currentTimeMillis() / 1000;

    public static final Map<String, ItemStack> eventIcons = new Object2ObjectOpenHashMap<>();

    static {
        eventIcons.put("Dark Auction", new ItemStack(Items.NETHER_BRICK));
        eventIcons.put("Bonus Fishing Festival", new ItemStack(Items.FISHING_ROD));
        eventIcons.put("Bonus Mining Fiesta", new ItemStack(Items.IRON_PICKAXE));
        eventIcons.put("Jacob's Farming Contest", new ItemStack(Items.IRON_HOE));
        eventIcons.put("New Year Celebration", new ItemStack(Items.CAKE));
        eventIcons.put("Election Over!", new ItemStack(Items.JUKEBOX));
        eventIcons.put("Election Booth Opens", new ItemStack(Items.JUKEBOX));
        eventIcons.put("Spooky Festival", new ItemStack(Items.JACK_O_LANTERN));
        eventIcons.put("Season of Jerry", new ItemStack(Items.SNOWBALL));
        eventIcons.put("Jerry's Workshop Opens", new ItemStack(Items.SNOW_BLOCK));
        eventIcons.put("Traveling Zoo", new ItemStack(Items.HAY_BLOCK)); // change to the custom head one day
    }

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(EventNotifications::timeUpdate, 20);

        SkyblockEvents.JOIN.register(EventNotifications::refreshEvents);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("skyblocker").then(
                        ClientCommandManager.literal("ye").then(
                                ClientCommandManager.argument("time", IntegerArgumentType.integer(6)).executes(context -> {
                                    MinecraftClient.getInstance().getToastManager().add(
                                            new EventToast(System.currentTimeMillis() / 1000 + context.getArgument("time", int.class), "Jacob's or something idk", new ItemStack(Items.PAPER))
                                    );
                                    return 0;
                                })
                        ).executes(context -> {
                            MinecraftClient.getInstance().getToastManager().add(
                                    new JacobEventToast(System.currentTimeMillis() / 1000 + 60, "Jacob's farming contest", new String[]{"Cactus","Cocoa Beans","Pumpkin"})
                            );
                            return 0;})
                )
        ));
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
        List<Integer> reminderTimes = SkyblockerConfigManager.get().general.eventNotifications.reminderTimes;
        if (reminderTimes.isEmpty()) return;

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
            String eventName = entry.getKey();

            for (Integer reminderTime : reminderTimes) {
                if (currentTime + reminderTime < skyblockEvent.start() && newTime + reminderTime >= skyblockEvent.start()) {
                    if (eventName.equals("Jacob's Farming Contest")) {
                        MinecraftClient.getInstance().getToastManager().add(
                                new JacobEventToast(skyblockEvent.start(), eventName, skyblockEvent.extras())
                        );
                    }
                    else {
                        MinecraftClient.getInstance().getToastManager().add(
                                new EventToast(skyblockEvent.start(), eventName, eventIcons.getOrDefault(eventName, new ItemStack(Items.PAPER)))
                        );
                    }
                }
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
