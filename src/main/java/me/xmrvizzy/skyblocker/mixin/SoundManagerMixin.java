package me.xmrvizzy.skyblocker.mixin;


import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    private MinecraftClient client = MinecraftClient.getInstance();

    @Inject(at = @At("HEAD"), method = "play(Lnet/minecraft/client/sound/SoundInstance;)V")
    private void play(SoundInstance sound, CallbackInfo ci) {
        if (sound.getId().toString().equals("minecraft:entity.player.splash")){
            if (client.player.fishHook != null)
                if (client.player.fishHook.isInOpenWater() && sound.getX() != client.player.getX() && sound.getY() != client.player.getY() && sound.getZ() != client.player.getZ() && SkyblockerConfig.get().fishing.enableFishingDing)
                    client.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        }
    }
}
