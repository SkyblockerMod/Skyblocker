package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.stp.SkyblockerBlockTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * This fails soft if the target class isn't found, so we don't need to worry about incompatible changes
 */
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask", remap = false)
@Pseudo
public class SodiumChunkBuilderMeshingTaskMixin {

	/**
	 * Fails soft if the injection point isn't found.
	 */
	@WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;", remap = true), remap = false, require = 0)
	private BakedModel skyblocker$modifyBakedModel(BlockModels blockModels, BlockState state, Operation<BakedModel> operation, @Local(ordinal = 0) BlockPos.Mutable pos) {
		if ((Utils.isOnSkyblock() || Debug.stpGlobal()) && SkyblockerConfigManager.get().uiAndVisuals.skyblockerTexturePredicates.blockTextures) {
			Identifier replacement = SkyblockerBlockTextures.getBlockReplacement(state.getBlock(), pos);

			if (replacement != null) {
				BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(replacement);

				return model != null ? model : operation.call(blockModels, state);
			}
		}

		return operation.call(blockModels, state);
	}
}
