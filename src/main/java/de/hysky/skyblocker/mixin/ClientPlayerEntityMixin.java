package de.hysky.skyblocker.mixin;

import com.mojang.authlib.GameProfile;

import dev.cbyrne.betterinject.annotations.Inject;
import de.hysky.skyblocker.skyblock.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.rift.HealingMelonIndicator;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void skyblocker$dropSelectedItem(CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isOnSkyblock()) {
            if (ItemProtection.isItemProtected(this.getInventory().getMainHandStack())) cir.setReturnValue(false);
            HotbarSlotLock.handleDropSelectedItem(this.getInventory().selectedSlot, cir);
        }
    }

    @Inject(method = "updateHealth", at = @At("RETURN"))
    public void skyblocker$updateHealth() {
        HealingMelonIndicator.updateHealth();
    }
}