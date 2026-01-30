package de.hysky.skyblocker.skyblock.slayers.boss.demonlord;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

@SuppressWarnings("unused")
public class MuteBlazeSounds {
	private static final ObjectSet<Identifier> MUTED_SOUNDS = ObjectSet.of(
			SoundEvents.BLAZE_BURN.location(),
			SoundEvents.BLAZE_AMBIENT.location(),
			SoundEvents.BLAZE_HURT.location(),
			SoundEvents.GHAST_SHOOT.location(),
			SoundEvents.FIRE_AMBIENT.location(),
			SoundEvents.LAVA_POP.location(),
			SoundEvents.LIGHTNING_BOLT_THUNDER.location(),
			SoundEvents.LIGHTNING_BOLT_IMPACT.location()
	);

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteBlazeSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (SkyblockerConfigManager.get().slayers.blazeSlayer.muteBlazeSounds && Utils.isInCrimson()) {
			return !MUTED_SOUNDS.contains(sound.location());
		}
		return true;
	}
}
