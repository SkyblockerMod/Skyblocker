package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.stp.SkyblockerBlockTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

/**
 * This is called directly by Sodium, and indirectly by Indigo and Vanilla (via BlockRenderManager).
 * 
 * If the Sodium specific Mixin gets applied fully then this Mixin will never be called, this serves as a fallback path in case the
 * Sodium Mixin isn't applied or if Indigo or Vanilla's block rendering path is being used instead.
 */
@Mixin(BlockModels.class)
public class BlockModelsMixin {

	@ModifyReturnValue(method = "getModel", at = @At("RETURN"))
	private BakedModel skyblocker$modifyBlockModel(BakedModel original, BlockState state) {
		if ((Utils.isOnSkyblock() || Debug.stpGlobal()) && SkyblockerConfigManager.get().uiAndVisuals.skyblockerTexturePredicates.blockTextures) {
			Identifier replacement = SkyblockerBlockTextures.getBlockReplacement(state.getBlock(), null);

			if (replacement != null) {
				BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(replacement);

				return model != null ? model : original;
			}
		}

		return original;
	}
}
