package de.hysky.skyblocker.skyblock.events;

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class EventNotifications {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final String JACOBS = "Jacob's Farming Contest";
	public static final String MAYOR_JERRY = "Mayor Jerry";

	public static final IntArrayList DEFAULT_REMINDERS = new IntArrayList(IntList.of(60, 60 * 5));
	public static final Map<String, FlexibleItemStack> eventIcons = Map.ofEntries(
			Map.entry("Dark Auction", new FlexibleItemStack(Items.NETHER_BRICK)),
			Map.entry("Bonus Fishing Festival", new FlexibleItemStack(Items.FISHING_ROD)),
			Map.entry("Bonus Mining Fiesta", new FlexibleItemStack(Items.IRON_PICKAXE)),
			Map.entry(JACOBS, new FlexibleItemStack(Items.IRON_HOE)),
			Map.entry("New Year Celebration", new FlexibleItemStack(Items.CAKE)),
			Map.entry("Election Over!", new FlexibleItemStack(Items.JUKEBOX)),
			Map.entry("Election Booth Opens", new FlexibleItemStack(Items.JUKEBOX)),
			Map.entry(MAYOR_JERRY, new FlexibleItemStack(Items.VILLAGER_SPAWN_EGG)),
			Map.entry("Spooky Festival", new FlexibleItemStack(Items.JACK_O_LANTERN)),
			Map.entry("Season of Jerry", new FlexibleItemStack(Items.SNOWBALL)),
			Map.entry("Jerry's Workshop Opens", new FlexibleItemStack(Items.SNOW_BLOCK)),
			Map.entry("Traveling Zoo", new FlexibleItemStack(Items.HAY_BLOCK)) // change to the custom head one day
	);
	private static final FlexibleItemStack FALLBACK_ICON = new FlexibleItemStack(Items.PAPER);
	private static long currentTime = System.currentTimeMillis() / 1000;

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(EventNotifications::timeUpdate, 20);
		SkyblockEvents.JOIN.register(EventNotifications::refreshEvents);
	}

	public static LiteralArgumentBuilder<FabricClientCommandSource> debugToasts() {
		return ClientCommands.literal("toasts").then(
				ClientCommands.argument("time", IntegerArgumentType.integer(0)).then(
						ClientCommands.argument("duration", IntegerArgumentType.integer(0)).then(
								ClientCommands.argument("jacob", BoolArgumentType.bool()).executes(context -> {
									long time = System.currentTimeMillis() / 1000 + context.getArgument("time", int.class);
									int duration = context.getArgument("duration", int.class);
									if (context.getArgument("jacob", Boolean.class)) {
										Minecraft.getInstance().getToastManager().addToast(
												new JacobEventToast(
														time,
														time + duration,
														"Jacob's farming contest",
														List.of("Cactus", "Cocoa Beans", "Pumpkin")
												)
										);
									} else {
										Minecraft.getInstance().getToastManager().addToast(
												new EventToast(
														time,
														time + duration,
														"Jacob's or something idk",
														FALLBACK_ICON
												)
										);
									}
									return 0;
								})
						)
				)
		);
	}

	private static final Map<String, LinkedList<SkyblockEvent>> events = new ConcurrentHashMap<>();

	public static Map<String, LinkedList<SkyblockEvent>> getEvents() {
		return events;
	}

	public static void refreshEvents() {
		CompletableFuture.supplyAsync(() -> {
			try {
				return Http.sendGetRequest("https://hysky.de/api/calendar");
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to download events list", e);
			}
			return null;
		}, Executors.newVirtualThreadPerTaskExecutor()).thenAccept(response -> {
			events.clear();
			if (response == null) {
				LOGGER.error("[Skyblocker] Failed to get events list");
				return;
			}

			List<SkyblockEvent> parsedEvents = SkyblockEvent.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getPartialOrThrow();
			for (SkyblockEvent event : parsedEvents) {
				if (event.start() + event.duration() < currentTime) continue;
				events.computeIfAbsent(event.event(), _ -> new LinkedList<>()).add(event);
			}

			for (Map.Entry<String, LinkedList<SkyblockEvent>> entry : events.entrySet()) {
				entry.getValue().sort(Comparator.comparingLong(SkyblockEvent::start)); // Sort just in case it's not in order for some reason in API
				//LOGGER.info("Next {} is at {}", entry.getKey(), entry.getValue().peekFirst());
			}

			SkyblockerConfigManager.update(config -> {
				for (String s : events.keySet()) {
					config.eventNotifications.eventsReminderTimes.computeIfAbsent(s, _ -> DEFAULT_REMINDERS);
				}
			});
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
					Minecraft instance = Minecraft.getInstance();
					if (eventName.equals(JACOBS) && skyblockEvent.extras().left().isPresent()) {
						instance.getToastManager().addToast(
								new JacobEventToast(
										skyblockEvent.start(),
										skyblockEvent.start() + skyblockEvent.duration(),
										eventName,
										skyblockEvent.extras().left().get()
								)
						);
					} else {
						instance.getToastManager().addToast(
								new EventToast(
										skyblockEvent.start(),
										skyblockEvent.start() + skyblockEvent.duration(),
										eventName,
										eventIcons.getOrDefault(eventName, FALLBACK_ICON)
								)
						);
					}
					SoundEvent soundEvent = SkyblockerConfigManager.get().eventNotifications.reminderSound.getSoundEvent();
					if (soundEvent != null)
						instance.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1f, 1f));
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

	public record SkyblockEvent(long start, int duration, String event, Either<List<String>, JerryPerks> extras, String warpCommand) {
		private static final Codec<SkyblockEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("timestamp").forGetter(SkyblockEvent::start),
				Codec.INT.fieldOf("duration").forGetter(SkyblockEvent::duration),
				Codec.STRING.fieldOf("event").forGetter(SkyblockEvent::event),
				Codec.either(Codec.STRING.listOf(), JerryPerks.CODEC)
						.fieldOf("extras").forGetter(SkyblockEvent::extras),
				Codec.STRING.fieldOf("location").forGetter(SkyblockEvent::warpCommand)
		).apply(instance, SkyblockEvent::new));

		public static final Codec<List<SkyblockEvent>> LIST_CODEC = CODEC.listOf();
	}

	public record JerryPerks(String mayorName, List<String> perks) {
		public static final Codec<JerryPerks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(JerryPerks::mayorName),
				Codec.STRING.listOf().fieldOf("perks").forGetter(JerryPerks::perks)
		).apply(instance, JerryPerks::new));
	}
}
