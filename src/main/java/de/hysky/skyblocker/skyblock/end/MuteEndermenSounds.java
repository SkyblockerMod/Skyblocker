package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Set;

@SuppressWarnings("unused")
public class MuteEndermenSounds {
	private static final Set<Identifier> MUTED_SOUNDS = Set.of(
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
