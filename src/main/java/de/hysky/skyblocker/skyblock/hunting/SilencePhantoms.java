package de.hysky.skyblocker.skyblock.hunting;

import net.minecraft.sounds.SoundEvent;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;

public class SilencePhantoms {

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(SilencePhantoms::onSound);
	}

	private static boolean shouldProcess() {
		if (Utils.isInGalatea()) {
			return SkyblockerConfigManager.get().hunting.moongladeMobs.silencePhantoms;
		} else if (Utils.isInSafari()) {
			return SkyblockerConfigManager.get().hunting.safari.silencePhantoms;
		}

		return false;
	}

	private static boolean onSound(SoundEvent sound) {
		return !(shouldProcess() && sound.location().getPath().startsWith("entity.phantom"));
	}
}
