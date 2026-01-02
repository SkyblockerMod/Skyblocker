package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class MuteEndermenSounds {
	private static final ObjectSet<Identifier> MUTED_SOUNDS = ObjectSet.of(
			SoundEvents.ENDERMAN_AMBIENT.location(),
			SoundEvents.ENDERMAN_DEATH.location(),
			SoundEvents.ENDERMAN_HURT.location(),
			SoundEvents.ENDERMAN_SCREAM.location(),
			SoundEvents.ENDERMAN_STARE.location()
	);

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteEndermenSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds && Utils.isInTheEnd()) {
			return !MUTED_SOUNDS.contains(sound.location());
		}
		return true;
	}
}
