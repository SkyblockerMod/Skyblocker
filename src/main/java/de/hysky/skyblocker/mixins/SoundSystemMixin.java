package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.LocationUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

	@Unique
	private static long lastWarningTime = 0;

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("TAIL"))
	private void onPlayTail(SoundInstance soundInstance, CallbackInfo ci) {
		if (SkyblockerConfigManager.get().slayers.miniBossSpawnAlert && Utils.isOnSkyblock() && soundInstance.getSound() != null && soundInstance.getId().equals(Identifier.ofVanilla("entity.generic.explode"))) {
			if (SlayerManager.isInSlayer() && SlayerManager.getSlayerQuest().isLfMinis() && soundInstance.getPitch() == 9 / 7f && soundInstance.getVolume() == 0.6f) {
				//Checks if MiniBoss is within a radius of the client's location
				if (LocationUtils.isWithinRadius(BlockPos.ofFloored(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()), 15)) {
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastWarningTime >= 1000) {
						TitleContainer.addTitle(new Title(Text.literal(I18n.translate("skyblocker.slayer.miniBossSpawnAlert")).formatted(Formatting.RED)), 20);
						MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
						lastWarningTime = currentTime;
					}
				}
			}
		}
	}

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
	private void onPlayHead(SoundInstance soundInstance, CallbackInfo ci) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds) {
			// Check if the sound identifier matches any Enderman sound identifiers
			if (soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_AMBIENT.id()) ||
					soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_DEATH.id()) ||
					soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_HURT.id()) ||
					soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.id()) ||
					soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_STARE.id())) {
				// Cancel the playback of Enderman sounds
				ci.cancel();
			}
		}
	}
}
