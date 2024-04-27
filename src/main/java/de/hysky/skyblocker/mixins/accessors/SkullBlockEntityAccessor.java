package de.hysky.skyblocker.mixins.accessors;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {
    @Invoker
    static CompletableFuture<Optional<GameProfile>> invokeFetchProfileByName(String name) {
        throw new UnsupportedOperationException();
    }
}
