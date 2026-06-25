package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.EventNotificationsConfig;
import de.hysky.skyblocker.config.screens.eventnotifications.EventConfigTimesEditScreen;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.LabelOption;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.OptionListener.UpdateType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventNotificationsCategory {

	private static boolean shouldPlaySound = false;

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		//return null;
		shouldPlaySound = false;
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/eventnotifications"))
				.name(Component.translatable("skyblocker.config.eventNotifications"))
				.option(Option.<EventNotificationsConfig.Criterion>createBuilder()
						.binding(defaults.eventNotifications.criterion,
								() -> config.eventNotifications.criterion,
								criterion -> config.eventNotifications.criterion = criterion)
						.controller(ConfigUtils.createEnumController())
						.name(Component.translatable("skyblocker.config.eventNotifications.criterion"))
						.build())
				.option(Option.<EventNotificationsConfig.Sound>createBuilder()
						.binding(defaults.eventNotifications.reminderSound,
								() -> config.eventNotifications.reminderSound,
								sound -> config.eventNotifications.reminderSound = sound)
						.controller(ConfigUtils.createEnumController())
						.name(Component.translatable("skyblocker.config.eventNotifications.notificationSound"))
						.listener((soundOption, event) -> {
							if (event == UpdateType.VALUE_CHANGE) {
								if (!shouldPlaySound) {
									shouldPlaySound = true;
									return;
								}
								if (soundOption.binding().get() != null)
									Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundOption.binding().get().getSoundEvent(), 1f, 1f));
							}
						})
						.build())
				.groups(createGroups(config))
				.build();

	}

	private static List<OptionGroup> createGroups(SkyblockerConfig config) {
		Map<String, EventNotificationsConfig.EventConfig> eventsReminderTimes = config.eventNotifications.events;
		if (eventsReminderTimes.isEmpty()) return List.of(OptionGroup.createBuilder().option(LabelOption.createBuilder().label(Component.translatable("skyblocker.config.eventNotifications.monologue")).build()).build());
		List<OptionGroup> groups = new ArrayList<>(eventsReminderTimes.size());
		for (Map.Entry<String, EventNotificationsConfig.EventConfig> entry : eventsReminderTimes.entrySet()) {
			groups.add(OptionGroup.createBuilder()
					.name(Component.literal(entry.getKey()))
					.option(Option.<Boolean>createBuilder()
							.name(Component.translatable("skyblocker.config.eventNotifications.event.enabled", entry.getKey()))
							.binding(EventNotifications.DEFAULT_REMINDERS.enabled,
									() -> entry.getValue().enabled,
									enabled -> entry.getValue().enabled = enabled
							)
							.controller(ConfigUtils.createBooleanController())
							.build()
					)
					.option(ButtonOption.createBuilder()
							.name(Component.translatable("skyblocker.config.eventNotifications.event.editReminders", entry.getKey()))
							.prompt(Component.translatable("skyblocker.config.eventNotifications.event.editReminders.prompt"))
							.description(Component.translatable("skyblocker.config.eventNotifications.event.editReminders.tooltip"))
							.action(s -> Minecraft.getInstance().gui.setScreen(new EventConfigTimesEditScreen(s, entry.getKey(), entry.getValue())))
							.build()
					)
					.build()
			);
		}
		return groups;
	}
}
