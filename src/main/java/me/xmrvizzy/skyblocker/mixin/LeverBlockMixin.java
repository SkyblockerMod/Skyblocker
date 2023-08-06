package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.skyblock.dungeon.OldLever;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cbyrne.betterinject.annotations.Arg;
import dev.cbyrne.betterinject.annotations.Inject;

@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin extends WallMountedBlock {
    protected LeverBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void skyblocker$onGetOutlineShape(@Arg BlockState state, CallbackInfoReturnable<VoxelShape> cir) {
        if (Utils.isOnSkyblock()) {
            VoxelShape shape = OldLever.getShape(state.get(FACE), state.get(FACING));
            if (shape != null) cir.setReturnValue(shape);
        }
    }
}
