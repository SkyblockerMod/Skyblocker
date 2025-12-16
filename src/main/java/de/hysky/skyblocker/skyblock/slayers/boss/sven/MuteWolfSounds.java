package de.hysky.skyblocker.skyblock.slayers.boss.sven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import net.minecraft.sound.SoundEvent;

@SuppressWarnings("unused")
public class MuteWolfSounds {
	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteWolfSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (SkyblockerConfigManager.get().slayers.wolfSlayer.muteWolfSounds && SlayerManager.isInSlayerQuestType(SlayerType.SVEN)) {
			return !sound.id().toString().contains("minecraft:entity.wolf.");
		}
		return true;
	}
}
