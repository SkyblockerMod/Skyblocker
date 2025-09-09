package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin extends Block {
	@Shadow
	protected static VoxelShape OUTLINE_SHAPE;

	@Unique
	private static final VoxelShape OLD_OUTLINE_SHAPE = VoxelShapes.fullCube();

	public CactusBlockMixin(Settings settings) {
		super(settings);
	}

	@ModifyReturnValue(method = "getOutlineShape", at = @At("RETURN"))
	private VoxelShape skyblocker$getOldCactusOutline(VoxelShape original) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hitbox.oldCactusHitbox ? OLD_OUTLINE_SHAPE : original;
	}

	@Override
	public VoxelShape getCullingShape(BlockState state) {
		return OUTLINE_SHAPE;
	}
}
