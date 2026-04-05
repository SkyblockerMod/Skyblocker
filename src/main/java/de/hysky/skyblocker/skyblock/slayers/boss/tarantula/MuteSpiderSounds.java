package de.hysky.skyblocker.skyblock.slayers.boss.tarantula;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class MuteSpiderSounds {
	private static final ObjectSet<Identifier> MUTED_SOUNDS = ObjectSet.of(
			SoundEvents.SPIDER_AMBIENT.location(),
			SoundEvents.SPIDER_DEATH.location(),
			SoundEvents.SPIDER_HURT.location(),
			SoundEvents.SPIDER_STEP.location(),
			SoundEvents.BAT_HURT.location(),
			SoundEvents.SILVERFISH_HURT.location(),
			SoundEvents.SILVERFISH_DEATH.location(),
			SoundEvents.SILVERFISH_AMBIENT.location(),
			SoundEvents.SKELETON_AMBIENT.location(),
			SoundEvents.SKELETON_DEATH.location(),
			SoundEvents.SKELETON_HURT.location(),
			SoundEvents.SKELETON_STEP.location()
	);

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteSpiderSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (SkyblockerConfigManager.get().slayers.spiderSlayer.muteSpiderSounds && (Utils.isInSpidersDen() || Utils.isInCrimson())) {
			// To keep blaze slayer skeleton daemon sounds
			if (!SlayerManager.isFightingSlayerType(SlayerType.DEMONLORD)) {
				return !MUTED_SOUNDS.contains(sound.location());
			}
		}
		return true;
	}
}
