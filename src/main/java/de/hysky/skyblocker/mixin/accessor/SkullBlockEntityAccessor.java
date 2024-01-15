package de.hysky.skyblocker.mixin.accessor;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {
    @Invoker
    static CompletableFuture<Optional<GameProfile>> invokeFetchProfile(String name) {
        throw new UnsupportedOperationException();
    }
}
