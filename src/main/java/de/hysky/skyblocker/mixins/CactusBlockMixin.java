package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin extends Block {
	public CactusBlockMixin(Settings settings) {
		super(settings);
	}

	@ModifyReturnValue(method = "getOutlineShape", at = @At("RETURN"))
	private VoxelShape skyblocker$getCactusOutline(VoxelShape original) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hitbox.oldCactusHitbox ? VoxelShapes.fullCube() : original;
	}
}
