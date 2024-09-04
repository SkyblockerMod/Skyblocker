package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.LocationUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance soundInstance, CallbackInfo ci) {
        if(SkyblockerConfigManager.get().slayers.miniBossSpawnAlert && Utils.isOnSkyblock() && soundInstance.getSound() != null && soundInstance.getId().equals(Identifier.ofVanilla("entity.generic.explode"))){
            if (Utils.isInSlayerQuest() && soundInstance.getPitch() == 9 / 7f && soundInstance.getVolume() == 0.6f) {
                //Checks if MiniBoss is within a radius of the client's location
                if(LocationUtils.isWithinRadius(BlockPos.ofFloored(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()), 15)){
                    Utils.Warning(I18n.translate("skyblocker.slayer.miniBossSpawnAlert"));
                }
            }
        }

        if(Utils.isInTheEnd() && SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds) {
            // Check if the sound identifier matches any Enderman sound identifiers
            if (soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_AMBIENT.getId()) ||
                    soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_DEATH.getId()) ||
                    soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_HURT.getId()) ||
                    soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.getId()) ||
                    soundInstance.getId().equals(SoundEvents.ENTITY_ENDERMAN_STARE.getId())) {

                // Cancel the playback of Enderman sounds
                ci.cancel();
            }
        }
    }
}