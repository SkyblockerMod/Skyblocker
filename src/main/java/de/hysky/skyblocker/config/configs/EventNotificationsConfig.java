package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;

public class EventNotificationsConfig {
    @SerialEntry
    public Criterion criterion = Criterion.SKYBLOCK;

    @SerialEntry
    public Sound reminderSound = Sound.PLING;

    @SerialEntry
    public Map<String, IntList> eventsReminderTimes = new HashMap<>();

    public enum Criterion {
        NONE,
        SKYBLOCK,
        HYPIXEL,
        EVERYWHERE;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.eventNotifications.criterion." + name());
        }
    }

    public enum Sound {
        NONE(null),
        BELL(SoundEvents.BLOCK_BELL_USE),
        DING(SoundEvents.ENTITY_ARROW_HIT_PLAYER),
        PLING(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()),
        GOAT(SoundEvents.GOAT_HORN_SOUNDS.getFirst().value());

        private final SoundEvent soundEvent;

        Sound(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
        }

        public SoundEvent getSoundEvent() {
            return soundEvent;
        }

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.eventNotifications.notificationSound.sound." + name());
        }
    }
}
