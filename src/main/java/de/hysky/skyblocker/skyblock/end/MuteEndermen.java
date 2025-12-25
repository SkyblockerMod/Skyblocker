package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class MuteEndermen {

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteEndermen::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds) {
			if (sound.location().equals(SoundEvents.ENDERMAN_AMBIENT.location()) ||
					sound.location().equals(SoundEvents.ENDERMAN_DEATH.location()) ||
					sound.location().equals(SoundEvents.ENDERMAN_HURT.location()) ||
					sound.location().equals(SoundEvents.ENDERMAN_SCREAM.location()) ||
					sound.location().equals(SoundEvents.ENDERMAN_STARE.location())) {
				return false;
			}
		}

		return true;
	}
}
