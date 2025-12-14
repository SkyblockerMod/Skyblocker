package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Set;

@SuppressWarnings("unused")
public class MuteEndermenSounds {
	private static final Set<Identifier> MUTED_SOUNDS = Set.of(
			SoundEvents.ENTITY_ENDERMAN_AMBIENT.id(),
			SoundEvents.ENTITY_ENDERMAN_DEATH.id(),
			SoundEvents.ENTITY_ENDERMAN_HURT.id(),
			SoundEvents.ENTITY_ENDERMAN_SCREAM.id(),
			SoundEvents.ENTITY_ENDERMAN_STARE.id()
	);

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteEndermenSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds) {
			return !MUTED_SOUNDS.contains(sound.id());
		}
		return true;
	}
}
