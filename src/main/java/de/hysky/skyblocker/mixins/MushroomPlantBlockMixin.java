package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.*;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MushroomPlantBlock.class)
public abstract class MushroomPlantBlockMixin extends Block {
	@Final
	@Shadow
	private static VoxelShape SHAPE;

	@Unique
	private static final VoxelShape skyblocker$OLD_SHAPE = Block.createCuboidShape(4.8, 0.0, 4.8, 11.2, 6.4, 11.2);

	public MushroomPlantBlockMixin(Settings settings) {
		super(settings);
	}

	@ModifyReturnValue(method = "getOutlineShape", at = @At("RETURN"))
	private VoxelShape getOldMushroomOutline(VoxelShape original) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hitbox.oldMushroomHitbox ? skyblocker$OLD_SHAPE : original;
	}

	@Override
	public VoxelShape getCullingShape(BlockState state) {
		return SHAPE;
	}
}
