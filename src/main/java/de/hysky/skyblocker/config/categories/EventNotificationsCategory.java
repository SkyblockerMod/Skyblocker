package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.utils.config.DurationController;
import dev.isxander.yacl3.api.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventNotificationsCategory {

    private static boolean shouldPlaySound = false;

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        shouldPlaySound = false;
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.autoconfig.skyblocker.option.eventNotifications"))
                .option(Option.<SkyblockerConfig.EventNotifications.Sound>createBuilder()
                        .binding(defaults.eventNotifications.reminderSound,
                                () -> config.eventNotifications.reminderSound,
                                sound -> config.eventNotifications.reminderSound = sound)
                        .controller(ConfigUtils::createEnumCyclingListController)
                        .name(Text.translatable("text.autoconfig.skyblocker.option.eventNotifications.notificationSound"))
                        .listener((soundOption, sound) -> {
                            if (!shouldPlaySound) {
                                shouldPlaySound = true;
                                return;
                            }
                            if (sound.getSoundEvent() != null)
                                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound.getSoundEvent(), 1f, 1f));
                        })
                        .build())
                .groups(createGroups(config))
                .build();

    }

    private static List<OptionGroup> createGroups(SkyblockerConfig config) {
        Map<String, List<Integer>> eventsReminderTimes = config.eventNotifications.eventsReminderTimes;
        List<OptionGroup> groups = new ArrayList<>(eventsReminderTimes.size());
        if (eventsReminderTimes.isEmpty()) return List.of(OptionGroup.createBuilder().option(LabelOption.create(Text.translatable("text.autoconfig.skyblocker.option.eventNotifications.monologue"))).build());
        for (Map.Entry<String, List<Integer>> entry : eventsReminderTimes.entrySet()) {
            groups.add(ListOption.<Integer>createBuilder()
                    .name(Text.literal(entry.getKey()))
                    .binding(EventNotifications.DEFAULT_REMINDERS, entry::getValue, entry::setValue)
                    .controller(option -> () -> new DurationController(option)) // yea
                            .description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.eventNotifications.@Tooltip[0]"),
                                    Text.empty(),
                                    Text.translatable("text.autoconfig.skyblocker.option.eventNotifications.@Tooltip[1]"),
                                    Text.empty(),
                                    Text.translatable("text.autoconfig.skyblocker.option.eventNotifications.@Tooltip[2]", entry.getKey())))
                    .initial(60)
                    .collapsed(true)
                    .build()
            );
        }
        return groups;
    }
}
