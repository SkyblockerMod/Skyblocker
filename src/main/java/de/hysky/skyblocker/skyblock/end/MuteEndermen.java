package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class MuteEndermen {

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteEndermen::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds) {
			if (sound.id().equals(SoundEvents.ENTITY_ENDERMAN_AMBIENT.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_DEATH.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_HURT.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_STARE.id())) {
				return false;
			}
		}

		return true;
	}
}
