package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block {
    @Shadow
    @Final
    protected static VoxelShape SHAPE;

    protected FarmlandBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyReturnValue(method = "getOutlineShape", at = @At("RETURN"))
    private VoxelShape skyblocker$replaceOutlineShape(VoxelShape original) {
        return Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hitbox.oldFarmlandHitbox ? VoxelShapes.fullCube() : original;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }
}
