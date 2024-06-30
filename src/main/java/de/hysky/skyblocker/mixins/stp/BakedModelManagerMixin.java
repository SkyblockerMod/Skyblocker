package de.hysky.skyblocker.mixins.stp;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.hysky.skyblocker.stp.ModelOverrideMerger;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {

	/**
	 * Inject before JSON models are resolved so that we can load ours before vanilla's are loaded, this happens async.
	 */
	@Inject(method = "method_45895", at = @At("HEAD"))
	private static void skyblocker$beforeJsonModelsFoundAsync(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, Resource>> cir) {
		ModelOverrideMerger.compileOverrides(manager);
	}

	/**
	 * Throwing an exception here is safe because the call we're targeting is wrapped in a try-catch block for IO purposes as well.
	 * (and the method we're calling is the same one that got the {@code original} Reader.)
	 */
	@ModifyVariable(method = "method_45898", at = @At("STORE"))
	private static Reader skyblocker$mergeModelOverrides(Reader original, Map.Entry<Identifier, Resource> entry) throws IOException {
		return ModelOverrideMerger.tryMerge(original, entry.getKey(), entry.getValue());
	}
}
