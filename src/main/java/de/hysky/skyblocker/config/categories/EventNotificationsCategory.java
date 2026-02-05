package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.EventNotificationsConfig;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.utils.config.DurationController;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.azureaaron.dandelion_bp.api.ConfigCategory;
import net.azureaaron.dandelion_bp.api.LabelOption;
import net.azureaaron.dandelion_bp.api.ListOption;
import net.azureaaron.dandelion_bp.api.Option;
import net.azureaaron.dandelion_bp.api.OptionGroup;
import net.azureaaron.dandelion_bp.api.OptionListener.UpdateType;
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
		Map<String, IntArrayList> eventsReminderTimes = config.eventNotifications.eventsReminderTimes;
		List<OptionGroup> groups = new ArrayList<>(eventsReminderTimes.size());
		if (eventsReminderTimes.isEmpty()) return List.of(OptionGroup.createBuilder().option(LabelOption.createBuilder().label(Component.translatable("skyblocker.config.eventNotifications.monologue")).build()).build());
		for (Map.Entry<String, IntArrayList> entry : eventsReminderTimes.entrySet()) {
			groups.add(ListOption.<Integer>createBuilder()
					.name(Component.literal(entry.getKey()))
					.binding(EventNotifications.DEFAULT_REMINDERS, entry::getValue, integers -> entry.setValue(new IntArrayList(integers)))
					.controller(new DurationController())
							.description(Component.translatable("skyblocker.config.eventNotifications.@Tooltip[0]"),
									Component.empty(),
									Component.translatable("skyblocker.config.eventNotifications.@Tooltip[1]"),
									Component.empty(),
									Component.translatable("skyblocker.config.eventNotifications.@Tooltip[2]", entry.getKey()))
					.initial(60)
					.collapsed(true)
					.build()
			);
		}
		return groups;
	}
}
