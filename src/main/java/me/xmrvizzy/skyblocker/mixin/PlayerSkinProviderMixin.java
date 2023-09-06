package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerSkinProvider.class)
public class PlayerSkinProviderMixin {
    /*@ModifyVariable(method = "loadSkin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/texture/PlayerSkinProvider$SkinTextureAvailableCallback;Z)V", at = @At("STORE"))
    private Runnable skyblocker$removeInvalidBase64LogSpam(Runnable runnable) {
        return Utils.isOnHypixel() ? () -> {
            try {
                runnable.run();
            } catch (IllegalArgumentException ignored) {
            }
        } : runnable;
    }*/
}
