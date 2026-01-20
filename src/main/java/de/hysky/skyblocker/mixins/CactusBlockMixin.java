package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin extends Block {
	@Shadow
	protected static VoxelShape SHAPE;

	@Unique
	private static final VoxelShape OLD_OUTLINE_SHAPE = Shapes.block();

	public CactusBlockMixin(Properties settings) {
		super(settings);
	}

	@ModifyReturnValue(method = "getShape", at = @At("RETURN"))
	private VoxelShape skyblocker$getOldCactusOutline(VoxelShape original) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hitbox.oldCactusHitbox ? OLD_OUTLINE_SHAPE : original;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state) {
		return SHAPE;
	}
}
