package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void skyblocker$handleInputEvents(CallbackInfo ci) {
        if (Utils.isOnSkyblock()) {
            HotbarSlotLock.handleInputEvents(player);
            ItemProtection.handleHotbarKeyPressed(player);
        }
    }

    @WrapOperation(method = "handleInputEvents", at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;"))
    private InventoryScreen skyblocker$skyblockInventoryScreen(PlayerEntity player, Operation<InventoryScreen> original) {
        return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? new SkyblockInventoryScreen(player) : original.call(player);
    }
}
