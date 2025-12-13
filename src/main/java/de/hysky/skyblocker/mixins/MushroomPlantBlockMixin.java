package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MushroomBlock.class)
public abstract class MushroomPlantBlockMixin extends Block {
	@Shadow
	protected static VoxelShape SHAPE;

	@Unique
	private static final VoxelShape OLD_SHAPE = Block.box(4.8, 0.0, 4.8, 11.2, 6.4, 11.2);

	public MushroomPlantBlockMixin(Properties settings) {
		super(settings);
	}

	@ModifyReturnValue(method = "getShape", at = @At("RETURN"))
	private VoxelShape skyblocker$getOldMushroomOutline(VoxelShape original) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hitbox.oldMushroomHitbox ? OLD_SHAPE : original;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state) {
		return SHAPE;
	}
}
