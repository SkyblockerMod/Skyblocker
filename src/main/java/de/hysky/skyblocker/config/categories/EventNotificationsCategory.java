package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.EventNotificationsConfig;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionListener.UpdateType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EventNotificationsCategory {

    private static boolean shouldPlaySound = false;

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
    	//return null;
        shouldPlaySound = false;
        return ConfigCategory.createBuilder()
        		.id(Identifier.of(SkyblockerMod.NAMESPACE, "config/eventnotifications"))
                .name(Text.translatable("skyblocker.config.eventNotifications"))
                .option(Option.<EventNotificationsConfig.Criterion>createBuilder()
                        .binding(defaults.eventNotifications.criterion,
                                () -> config.eventNotifications.criterion,
                                criterion -> config.eventNotifications.criterion = criterion)
                        .controller(ConfigUtils.createEnumController())
                        .name(Text.translatable("skyblocker.config.eventNotifications.criterion"))
                        .build())
                .option(Option.<EventNotificationsConfig.Sound>createBuilder()
                        .binding(defaults.eventNotifications.reminderSound,
                                () -> config.eventNotifications.reminderSound,
                                sound -> config.eventNotifications.reminderSound = sound)
                        .controller(ConfigUtils.createEnumController())
                        .name(Text.translatable("skyblocker.config.eventNotifications.notificationSound"))
                        .listener((soundOption, event) -> {
                        	if (event == UpdateType.VALUE_CHANGE) {
                                if (!shouldPlaySound) {
                                    shouldPlaySound = true;
                                    return;
                                }
                                if (soundOption.binding().get() != null)
                                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(soundOption.binding().get().getSoundEvent(), 1f, 1f));
                        	}
                        })
                        .build())
                //.groups(createGroups(config))
                .build();

    }

    /*private static List<OptionGroup> createGroups(SkyblockerConfig config) {
        Map<String, IntList> eventsReminderTimes = config.eventNotifications.eventsReminderTimes;
        List<OptionGroup> groups = new ArrayList<>(eventsReminderTimes.size());
        if (eventsReminderTimes.isEmpty()) return List.of(OptionGroup.createBuilder().option(LabelOption.create(Text.translatable("skyblocker.config.eventNotifications.monologue"))).build());
        for (Map.Entry<String, IntList> entry : eventsReminderTimes.entrySet()) {
            groups.add(ListOption.<Integer>createBuilder()
                    .name(Text.literal(entry.getKey()))
                    .binding(EventNotifications.DEFAULT_REMINDERS, entry::getValue, integers -> entry.setValue(new IntImmutableList(integers)))
                    .controller(option -> () -> new DurationController(option)) // yea
                            .description(OptionDescription.of(Text.translatable("skyblocker.config.eventNotifications.@Tooltip[0]"),
                                    Text.empty(),
                                    Text.translatable("skyblocker.config.eventNotifications.@Tooltip[1]"),
                                    Text.empty(),
                                    Text.translatable("skyblocker.config.eventNotifications.@Tooltip[2]", entry.getKey())))
                    .initial(60)
                    .collapsed(true)
                    .build()
            );
        }
        return groups;
    }*/
}
