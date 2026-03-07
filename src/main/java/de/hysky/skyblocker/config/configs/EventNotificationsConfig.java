package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.annotations.GenToString;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

public class EventNotificationsConfig {
	public Criterion criterion = Criterion.SKYBLOCK;

	public Sound reminderSound = Sound.PLING;

	public Map<String, EventConfig> events = new HashMap<>();

	public enum Criterion {
		NONE,
		SKYBLOCK,
		HYPIXEL,
		EVERYWHERE;

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.eventNotifications.criterion." + name());
		}
	}

	public static class EventConfig {
		public boolean enabled;
		public IntArrayList reminderTimes;

		public EventConfig() {
			enabled = true;
			reminderTimes = new IntArrayList();
		}

		@GenToString
		@Override
		public native String toString();
	}

	public enum Sound {
		NONE(null),
		BELL(SoundEvents.BELL_BLOCK),
		DING(SoundEvents.ARROW_HIT_PLAYER),
		PLING(SoundEvents.NOTE_BLOCK_PLING.value()),
		GOAT(SoundEvents.GOAT_HORN_SOUND_VARIANTS.getFirst().value());

		private final @Nullable SoundEvent soundEvent;

		Sound(@Nullable SoundEvent soundEvent) {
			this.soundEvent = soundEvent;
		}

		public @Nullable SoundEvent getSoundEvent() {
			return soundEvent;
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.eventNotifications.notificationSound.sound." + name());
		}
	}
}
