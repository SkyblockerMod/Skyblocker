package de.hysky.skyblocker.skyblock.slayers.boss.demonlord;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Set;

@SuppressWarnings("unused")
public class MuteBlazeSounds {
	private static final Set<Identifier> MUTED_SOUNDS = Set.of(
			SoundEvents.ENTITY_BLAZE_BURN.id(),
			SoundEvents.ENTITY_BLAZE_AMBIENT.id(),
			SoundEvents.ENTITY_BLAZE_HURT.id(),
			SoundEvents.ENTITY_GHAST_SHOOT.id(),
			SoundEvents.BLOCK_FIRE_AMBIENT.id(),
			SoundEvents.BLOCK_LAVA_POP.id(),
			SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER.id(),
			SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT.id()
	);

	@Init
	public static void init() {
		PlaySoundEvents.ALLOW_SOUND.register(MuteBlazeSounds::onSound);
	}

	private static boolean onSound(SoundEvent sound) {
		if (Utils.isInCrimson() &&
				SkyblockerConfigManager.get().slayers.blazeSlayer.muteBlazeSounds &&
				SlayerManager.isInSlayerQuestType(SlayerType.DEMONLORD)) {
			return !MUTED_SOUNDS.contains(sound.id());
		}
		return true;
	}
}
