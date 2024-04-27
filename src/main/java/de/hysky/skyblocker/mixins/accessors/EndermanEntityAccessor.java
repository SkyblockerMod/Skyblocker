package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.block.BlockState;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(EndermanEntity.class)
public interface EndermanEntityAccessor {
    @Accessor
    static TrackedData<Optional<BlockState>> getCARRIED_BLOCK() {
        throw new UnsupportedOperationException();
    }
}
