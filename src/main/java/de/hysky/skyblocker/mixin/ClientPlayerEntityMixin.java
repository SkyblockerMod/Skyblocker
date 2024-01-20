package de.hysky.skyblocker.mixin;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.rift.HealingMelonIndicator;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow @Final protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void skyblocker$dropSelectedItem(CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isOnSkyblock()) {
            if (ItemProtection.isItemProtected(this.getInventory().getMainHandStack())) {
                if (!SkyblockerConfigManager.get().locations.dungeons.allowDroppingProtectedItems
                        || (SkyblockerConfigManager.get().locations.dungeons.allowDroppingProtectedItems && !Utils.isInDungeons())) {
                    cir.setReturnValue(false);
                }
            }
            HotbarSlotLock.handleDropSelectedItem(this.getInventory().selectedSlot, cir);
        }
    }

    @Inject(method = "updateHealth", at = @At("RETURN"))
    public void skyblocker$updateHealth(CallbackInfo ci) {
        HealingMelonIndicator.updateHealth();
    }

    @Inject(method = "openEditSignScreen", at = @At("HEAD"), cancellable = true)
    public void skyblocker$partyFinderRange(SignBlockEntity sign, boolean front, CallbackInfo callbackInfo) {
        if (PartyFinderScreen.isInKuudraPartyFinder) return;
        if (client.currentScreen instanceof PartyFinderScreen partyFinderScreen && !partyFinderScreen.isAborted()) {
            if (sign.getText(front).getMessage(3, false).getString().toLowerCase().contains("level")) {
                partyFinderScreen.updateSign(sign, front);
                callbackInfo.cancel();
            }
        }
    }
}