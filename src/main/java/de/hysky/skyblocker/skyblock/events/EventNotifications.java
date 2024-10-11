package de.hysky.skyblocker.skyblock.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EventNotifications {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static long currentTime = System.currentTimeMillis() / 1000;

    public static final String JACOBS = "Jacob's Farming Contest";

    public static final IntList DEFAULT_REMINDERS = IntList.of(60, 60 * 5);

    public static final Map<String, ItemStack> eventIcons = Map.ofEntries(
            Map.entry("Dark Auction", new ItemStack(Items.NETHER_BRICK)),
            Map.entry("Bonus Fishing Festival", new ItemStack(Items.FISHING_ROD)),
            Map.entry("Bonus Mining Fiesta", new ItemStack(Items.IRON_PICKAXE)),
            Map.entry(JACOBS, new ItemStack(Items.IRON_HOE)),
            Map.entry("New Year Celebration", new ItemStack(Items.CAKE)),
            Map.entry("Election Over!", new ItemStack(Items.JUKEBOX)),
            Map.entry("Election Booth Opens", new ItemStack(Items.JUKEBOX)),
            Map.entry("Spooky Festival", new ItemStack(Items.JACK_O_LANTERN)),
            Map.entry("Season of Jerry", new ItemStack(Items.SNOWBALL)),
            Map.entry("Jerry's Workshop Opens", new ItemStack(Items.SNOW_BLOCK)),
            Map.entry("Traveling Zoo", new ItemStack(Items.HAY_BLOCK)) // change to the custom head one day
    );

    @Init
    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(EventNotifications::timeUpdate, 20);

        SkyblockEvents.JOIN.register(EventNotifications::refreshEvents);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("skyblocker").then(
                        ClientCommandManager.literal("debug").then(
                                ClientCommandManager.literal("toasts").then(
                                        ClientCommandManager.argument("time", IntegerArgumentType.integer(0))
                                                .then(ClientCommandManager.argument("jacob", BoolArgumentType.bool()).executes(context -> {
                                                                    long time = System.currentTimeMillis() / 1000 + context.getArgument("time", int.class);
                                                                    if (context.getArgument("jacob", Boolean.class)) {
                                                                        MinecraftClient.getInstance().getToastManager().add(
                                                                                new JacobEventToast(time, "Jacob's farming contest", new String[]{"Cactus", "Cocoa Beans", "Pumpkin"})
                                                                        );
                                                                    } else {
                                                                        MinecraftClient.getInstance().getToastManager().add(
                                                                                new EventToast(time, "Jacob's or something idk", new ItemStack(Items.PAPER))
                                                                        );
                                                                    }
                                                                    return 0;
                                                                }
                                                        )
                                                )
                                )
                        )
                )
        ));
    }

    private static final Map<String, LinkedList<SkyblockEvent>> events = new ConcurrentHashMap<>();

    public static Map<String, LinkedList<SkyblockEvent>> getEvents() {
        return events;
    }

    public static void refreshEvents() {
        CompletableFuture.supplyAsync(() -> {
            try {
                JsonArray jsonElements = SkyblockerMod.GSON.fromJson(Http.sendGetRequest("https://hysky.de/api/calendar"), JsonArray.class);
                return jsonElements.asList().stream().map(JsonElement::getAsJsonObject).toList();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to download events list", e);
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

            for (String s : events.keySet()) {
                SkyblockerConfigManager.get().eventNotifications.eventsReminderTimes.computeIfAbsent(s, s1 -> DEFAULT_REMINDERS);
            }
        }).exceptionally(EventNotifications::itBorked);
    }

    private static Void itBorked(Throwable throwable) {
        LOGGER.error("[Skyblocker] Event loading borked, sowwy :(", throwable);
        return null;
    }


    private static void timeUpdate() {

        long newTime = System.currentTimeMillis() / 1000;
        for (Map.Entry<String, LinkedList<SkyblockEvent>> entry : events.entrySet()) {
            LinkedList<SkyblockEvent> nextEvents = entry.getValue();
            SkyblockEvent skyblockEvent = nextEvents.peekFirst();
            if (skyblockEvent == null) continue;

            // Remove finished event
            if (newTime > skyblockEvent.start() + skyblockEvent.duration()) {
                nextEvents.pollFirst();
                skyblockEvent = nextEvents.peekFirst();
                if (skyblockEvent == null) continue;
            }
            String eventName = entry.getKey();
            // Cannot be changed to fast util due to casting issues
            List<Integer> reminderTimes = SkyblockerConfigManager.get().eventNotifications.eventsReminderTimes.getOrDefault(eventName, DEFAULT_REMINDERS);
            if (reminderTimes.isEmpty()) continue;

            for (int reminderTime : reminderTimes) {
                if (criterionMet() && currentTime + reminderTime < skyblockEvent.start() && newTime + reminderTime >= skyblockEvent.start()) {
                    MinecraftClient instance = MinecraftClient.getInstance();
                    if (eventName.equals(JACOBS)) {
                        instance.getToastManager().add(
                                new JacobEventToast(skyblockEvent.start(), eventName, skyblockEvent.extras())
                        );
                    } else {
                        instance.getToastManager().add(
                                new EventToast(skyblockEvent.start(), eventName, eventIcons.getOrDefault(eventName, new ItemStack(Items.PAPER)))
                        );
                    }
                    SoundEvent soundEvent = SkyblockerConfigManager.get().eventNotifications.reminderSound.getSoundEvent();
                    if (soundEvent != null)
                        instance.getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1f, 1f));
                    break;
                }
            }
        }
        currentTime = newTime;
    }

    private static boolean criterionMet() {
        return switch (SkyblockerConfigManager.get().eventNotifications.criterion) {
            case NONE -> false;
            case SKYBLOCK -> Utils.isOnSkyblock();
            case HYPIXEL -> Utils.isOnHypixel();
            case EVERYWHERE -> true;
        };
    }

    public record SkyblockEvent(long start, int duration, String[] extras, @Nullable String warpCommand) {
        public static SkyblockEvent of(JsonObject jsonObject) {
            String location = jsonObject.get("location").getAsString();
            location = location.isBlank() ? null : location;
            return new SkyblockEvent(jsonObject.get("timestamp").getAsLong(),
                    jsonObject.get("duration").getAsInt(),
                    jsonObject.get("extras").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toArray(String[]::new),
                    location);
        }
    }
}
