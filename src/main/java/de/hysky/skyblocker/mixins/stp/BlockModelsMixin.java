package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.stp.SkyblockerBlockTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

/**
 * This is called directly by Sodium, and indirectly by Indigo (via BlockRenderManager).
 */
@Mixin(BlockModels.class)
public class BlockModelsMixin {

	@ModifyReturnValue(method = "getModel", at = @At("RETURN"))
	private BakedModel skyblocker$modifyBlockModel(BakedModel original, @Local(argsOnly = true) BlockState state) {
		if (Utils.isOnHypixel()) {
			Identifier replacement = SkyblockerBlockTextures.getBlockReplacement(state.getBlock());

			if (replacement != null) {
				BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(replacement);

				return model != null ? model : original;
			}
		}

		return original;
	}
}
