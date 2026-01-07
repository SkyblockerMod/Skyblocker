package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.dungeon.OldLever;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin extends FaceAttachedHorizontalDirectionalBlock {
	protected LeverBlockMixin(Properties settings) {
		super(settings);
	}

	@Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
	public void skyblocker$onGetOutlineShape(CallbackInfoReturnable<VoxelShape> cir, @Local(argsOnly = true) BlockState state) {
		if (Utils.isOnSkyblock()) {
			VoxelShape shape = OldLever.getShape(state.getValue(FACE), state.getValue(FACING));
			if (shape != null) cir.setReturnValue(shape);
		}
	}
}
