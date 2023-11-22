package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import dev.cbyrne.betterinject.annotations.Inject;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void skyblocker$handleInputEvents() {
        if (Utils.isOnSkyblock()) {
            HotbarSlotLock.handleInputEvents(player);
        }
    }
}