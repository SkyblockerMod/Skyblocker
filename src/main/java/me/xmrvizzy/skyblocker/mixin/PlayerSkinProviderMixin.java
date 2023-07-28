package me.xmrvizzy.skyblocker.mixin;

import java.util.concurrent.ExecutorService;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.texture.PlayerSkinProvider;

@Mixin(PlayerSkinProvider.class)
public class PlayerSkinProviderMixin {

	@Redirect(method = "loadSkin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/texture/PlayerSkinProvider$SkinTextureAvailableCallback;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ExecutorService;execute(Ljava/lang/Runnable;)V", remap = false))
	private void skyblocker$removeInvalidBase64LogSpam(ExecutorService executor, Runnable runnable) {
		executor.execute(() -> {
			try {
				runnable.run();
			} catch (Throwable t) {
				if (!(t instanceof IllegalArgumentException) || !Utils.isOnHypixel()) {
					t.printStackTrace();
				}
			}
		});
	}
}
