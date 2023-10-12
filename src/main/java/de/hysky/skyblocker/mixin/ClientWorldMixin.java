package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.skyblock.CrystalWishingCompassSolver;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(
        at = @At("HEAD"),
        method = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V"
    )
    public void skyblocker$handleParticles(
            ParticleEffect effect, boolean alwaysSpawn, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        CrystalWishingCompassSolver.getInstance().onSpawnParticle(effect.getType(), x, y, z);
    }
}
