package de.hysky.skyblocker.skyblock.slayers.boss.sven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sounds.SoundEvent;

public class MuteWolfSounds {
	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteWolfSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (SkyblockerConfigManager.get().slayers.wolfSlayer.muteWolfSounds && (Utils.isInPark() || Utils.isInHub())) {
			return !sound.location().toString().contains("minecraft:entity.wolf.");
		}
		return true;
	}
}
