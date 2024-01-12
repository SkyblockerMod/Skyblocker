package de.hysky.skyblocker.mixin.accessor;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {
    @Contract(pure = true)
    @Accessor("userCache")
    public static @Nullable LoadingCache<String, CompletableFuture<Optional<GameProfile>>> getUserCache() {
        return null;
    }

    @Invoker
    public static CompletableFuture<Optional<GameProfile>> invokeFetchProfile(String name) {
        return new CompletableFuture<>();
    }
}
