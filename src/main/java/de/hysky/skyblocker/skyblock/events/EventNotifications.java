package de.hysky.skyblocker.skyblock.events;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.EventNotificationsConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class EventNotifications {
	public static final EventNotificationsConfig.EventConfig DEFAULT_REMINDERS = new EventNotificationsConfig.EventConfig();
	private static Instant previousTime = Instant.now();

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(EventNotifications::timeUpdate, 20);
		de.hysky.skyblocker.events.SkyblockEvents.JOIN.register(CalendarEvents::refreshEvents);
	}

	public static LiteralArgumentBuilder<FabricClientCommandSource> debugToasts() {
		return ClientCommands.literal("toasts").then(
				ClientCommands.argument("time", IntegerArgumentType.integer(0)).then(
						ClientCommands.argument("duration", IntegerArgumentType.integer(0)).then(
								ClientCommands.argument("jacob", BoolArgumentType.bool()).executes(context -> {
									long time = System.currentTimeMillis() / 1000 + context.getArgument("time", int.class);
									int duration = context.getArgument("duration", int.class);
									if (context.getArgument("jacob", Boolean.class)) {
										Minecraft.getInstance().gui.toastManager().addToast(
												new JacobEventToast(
														new EventInstance(SkyblockEvents.JACOBS_FARMING_CONTEST, Instant.ofEpochSecond(time), Duration.ofSeconds(duration), Optional.empty(), EventInstance.AdditionalInfo.EMPTY),
														new ExtraEventData.Jacobs(List.of("Cactus", "Cocoa Beans", "Pumpkin"))
												)
										);
									} else {
										Minecraft.getInstance().gui.toastManager().addToast(
												new EventToast(
														new EventInstance(SkyblockEvents.JACOBS_FARMING_CONTEST, Instant.ofEpochSecond(time), Duration.ofSeconds(duration), Optional.empty(), EventInstance.AdditionalInfo.EMPTY)
												)
										);
									}
									return 0;
								})
						)
				)
		);
	}


	private static void timeUpdate() {
		Instant now = Instant.now();
		for (SkyblockEvent event : SkyblockEvents.getAllEvents()) {
			if (!criterionMet()) continue;
			String eventName = event.name();
			EventNotificationsConfig.EventConfig config = SkyblockerConfigManager.get().eventNotifications.events.getOrDefault(eventName, DEFAULT_REMINDERS);
			if (!config.enabled) continue;
			Optional<EventInstance> instanceOptional = EventManager.getNext(event, previousTime, false);
			if (instanceOptional.isEmpty()) continue;
			EventInstance instance = instanceOptional.get();

			for (int reminderTime : config.reminderTimes) {
				// Only show notification if last time we ticked was before the event, and we are now after the event start
				if (now.plusSeconds(reminderTime).isBefore(instance.start()) || previousTime.plusSeconds(reminderTime).isAfter(instance.start())) continue;
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.gui.toastManager().addToast(instance.createToast());
				SoundEvent soundEvent = SkyblockerConfigManager.get().eventNotifications.reminderSound.getSoundEvent();
				if (soundEvent != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1f, 1f));
				break;
			}
		}
		previousTime = now;
	}

	private static boolean criterionMet() {
		return switch (SkyblockerConfigManager.get().eventNotifications.criterion) {
			case NONE -> false;
			case SKYBLOCK -> Utils.isOnSkyblock();
			case HYPIXEL -> Utils.isOnHypixel();
			case EVERYWHERE -> true;
		};
	}

	/**
	 * An event with info that changes every event.
	 */
	public record DetailedEvent(EventInstance event, Either<List<String>, ExtraEventData.JerryPerks> extras) {
		private static final Codec<DetailedEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EventInstance.MAP_CODEC.fieldOf("event").forGetter(DetailedEvent::event),
				Codec.either(Codec.STRING.listOf(), ExtraEventData.JerryPerks.CODEC)
						.fieldOf("extras").forGetter(DetailedEvent::extras)
		).apply(instance, DetailedEvent::new));

		public static final Codec<List<DetailedEvent>> LIST_CODEC = CODEC.listOf();

		public Instant start() {
			return event.start();
		}

		public Duration duration() {
			return event.duration();
		}

		public Instant end() {
			return event.end();
		}

		public Optional<String> warpCommand() {
			return event.additionalInfo().warpCommand();
		}
	}

}
