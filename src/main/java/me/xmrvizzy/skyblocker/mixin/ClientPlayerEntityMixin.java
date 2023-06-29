package me.xmrvizzy.skyblocker.mixin;

import com.mojang.authlib.GameProfile;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.rift.HealingMelonIndicator;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void skyblocker$dropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isOnSkyblock()) HotbarSlotLock.handleDropSelectedItem(this.getInventory().selectedSlot, cir);
    }

    @Inject(method = "updateHealth", at = @At("HEAD"))
    public void skyblocker$updateHealth(float health, CallbackInfo info) {
        HealingMelonIndicator.updateHealth(MinecraftClient.getInstance());
    }
}