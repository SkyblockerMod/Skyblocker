package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.utils.config.DurationController;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventNotificationsCategory {

    public static ConfigCategory create(SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.literal("Event Notifications"))
                .groups(createGroups(config))
                .build();

    }

    private static List<OptionGroup> createGroups(SkyblockerConfig config) {
        Map<String, List<Integer>> eventsReminderTimes = config.eventNotifications.eventsReminderTimes;
        List<OptionGroup> groups = new ArrayList<>(eventsReminderTimes.size());
        for (Map.Entry<String, List<Integer>> entry : eventsReminderTimes.entrySet()) {
            groups.add(ListOption.<Integer>createBuilder()
                    .name(Text.literal(entry.getKey()))
                    .binding(EventNotifications.DEFAULT_REMINDERS, entry::getValue, entry::setValue)
                    .controller(option -> () -> new DurationController(option)) // yea
                            .description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.eventNotifications.@Tooltip[0]"),
                                    Text.empty(),
                                    Text.translatable("text.autoconfig.skyblocker.option.general.eventNotifications.@Tooltip[1]"),
                                    Text.empty(),
                                    Text.translatable("text.autoconfig.skyblocker.option.general.eventNotifications.@Tooltip[2]", entry.getKey())))
                    .initial(60)
                    .collapsed(true)
                    .build()
            );
        }
        return groups;
    }
}
