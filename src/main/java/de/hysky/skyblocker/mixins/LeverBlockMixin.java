package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.dungeon.OldLever;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin extends WallMountedBlock {
    protected LeverBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void skyblocker$onGetOutlineShape(CallbackInfoReturnable<VoxelShape> cir, @Local(argsOnly = true) BlockState state) {
        if (Utils.isOnSkyblock()) {
            VoxelShape shape = OldLever.getShape(state.get(FACE), state.get(FACING));
            if (shape != null) cir.setReturnValue(shape);
        }
    }
}
