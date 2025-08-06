package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;

public class SilencePhantoms {

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(SilencePhantoms::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (Utils.isInGalatea() && SkyblockerConfigManager.get().hunting.huntingMobs.silencePhantoms && sound.id().getPath().startsWith("entity.phantom")) {
			return false;
		}

		return true;
	}
}
