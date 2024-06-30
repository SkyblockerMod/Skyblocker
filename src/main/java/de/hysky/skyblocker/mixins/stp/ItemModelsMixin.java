package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.stp.SkyblockerItemTextures;
import de.hysky.skyblocker.stp.SkyblockerUniversalTextures;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
	@Unique
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	@Unique
	private static final Int2ObjectMap<Identifier> UNIVERSAL_TEXTURE_CACHE = SkyblockerUniversalTextures.UNIVERSAL_TEXTURE_CACHE;

	@ModifyReturnValue(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;", at = @At("RETURN"))
	private BakedModel skyblocker$overrideSkyblockItemModel(BakedModel original, ItemStack stack) {
		UIAndVisualsConfig.STP config = SkyblockerConfigManager.get().uiAndVisuals.skyblockerTexturePredicates;

		//Skyblock Textures
		if ((Utils.isOnSkyblock() || Debug.stpGlobal()) && config.skyblockItemTextures) {
			String skyblockId = stack.getSkyblockId();

			if (!skyblockId.isEmpty()) {
				Identifier modelId = SkyblockerItemTextures.getModelId(skyblockId);

				if (modelId != null) {
					BakedModel model = CLIENT.getBakedModelManager().getModel(modelId);

					if (model != null) return model;
				}
			}
		}

		//Universal Textures
		if (config.universalItemTextures && (Utils.isOnHypixel() || Debug.stpGlobal())) {
			int hash = System.identityHashCode(stack);

			if (UNIVERSAL_TEXTURE_CACHE.containsKey(hash)) {
				//We only cache non-null models so we can skip the null check on that
				Identifier cachedId = UNIVERSAL_TEXTURE_CACHE.get(hash);

				if (cachedId != null) return CLIENT.getBakedModelManager().getModel(cachedId);
			} else {
				Identifier modelId = SkyblockerUniversalTextures.getUniversalModel(stack);

				if (modelId != null) {
					BakedModel model = CLIENT.getBakedModelManager().getModel(modelId);

					if (model != null) {
						UNIVERSAL_TEXTURE_CACHE.put(hash, modelId);

						return model;
					}
				} else {
					UNIVERSAL_TEXTURE_CACHE.put(hash, null);
				}
			}
		}

		return original;
	}

	@Inject(method = "reloadModels", at = @At("HEAD"))
	private void skyblocker$clearCache(CallbackInfo ci) {
		UNIVERSAL_TEXTURE_CACHE.clear();
	}
}
