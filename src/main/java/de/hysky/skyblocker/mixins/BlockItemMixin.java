package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.garden.GreenhousePaste;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place", at = @At("TAIL"))
	private void afterPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (!cir.getReturnValue().consumesAction()) return;

		Level level = context.getLevel();
		if (level.isClientSide()) return;

		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		BlockState state = level.getBlockState(pos);

		GreenhousePaste.onBlockChange(pos);
	}
}
