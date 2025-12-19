package de.hysky.skyblocker.skyblock.slayers.boss.tarantula;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Set;

@SuppressWarnings("unused")
public class MuteSpiderSounds {
	private static final Set<Identifier> MUTED_SOUNDS = Set.of(
			SoundEvents.ENTITY_SPIDER_AMBIENT.id(),
			SoundEvents.ENTITY_SPIDER_DEATH.id(),
			SoundEvents.ENTITY_SPIDER_HURT.id(),
			SoundEvents.ENTITY_SPIDER_STEP.id(),
			SoundEvents.ENTITY_BAT_HURT.id(),
			SoundEvents.ENTITY_SILVERFISH_HURT.id(),
			SoundEvents.ENTITY_SILVERFISH_DEATH.id(),
			SoundEvents.ENTITY_SILVERFISH_AMBIENT.id(),
			SoundEvents.ENTITY_SKELETON_AMBIENT.id(),
			SoundEvents.ENTITY_SKELETON_DEATH.id(),
			SoundEvents.ENTITY_SKELETON_HURT.id(),
			SoundEvents.ENTITY_SKELETON_STEP.id()
	);

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteSpiderSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (SkyblockerConfigManager.get().slayers.spiderSlayer.muteSpiderSounds && (Utils.isInSpidersDen() || Utils.getArea() == Area.BURNING_DESERT)) {
			return !MUTED_SOUNDS.contains(sound.id());
		}
		return true;
	}
}
