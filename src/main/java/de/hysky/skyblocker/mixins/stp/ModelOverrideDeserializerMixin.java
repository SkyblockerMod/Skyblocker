package de.hysky.skyblocker.mixins.stp;

import java.util.Map;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerTexturePredicates;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.util.Identifier;

/**
 * Hooks the vanilla model loading process and returns placeholder floats for our own predicates so that the system doesn't explode.
 */
@Mixin(targets = "net.minecraft.client.render.model.json.ModelOverride$Deserializer")
public class ModelOverrideDeserializerMixin {

	@ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
	private ModelOverride skyblocker$injectModelOverrides(ModelOverride original, @Local JsonObject overrides) {
		original.setItemPredicates(SkyblockerTexturePredicates.compilePredicates(overrides));

		return original;
	}

	@WrapOperation(method = "deserializeMinPropertyValues", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;asFloat(Lcom/google/gson/JsonElement;Ljava/lang/String;)F"))
	private float skyblocker$returnPlaceholderFloat(JsonElement element, String name, Operation<Float> operation, @Share("hasPredicate") LocalBooleanRef hasPredicate) {
		if (name.startsWith(SkyblockerMod.NAMESPACE)) {
			hasPredicate.set(true);

			return 0f;
		} else {
			return operation.call(element, name);
		}
	}

	@ModifyExpressionValue(method = "deserializeMinPropertyValues", at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;", remap = false))
	private Stream<Map.Entry<Identifier, Float>> skyblocker$hideSkyblockerPredicates(Stream<Map.Entry<Identifier, Float>> stream, @Share("hasPredicate") LocalBooleanRef hasPredicate) {
		if (hasPredicate.get()) {
			return stream.filter(e -> !e.getKey().getNamespace().equals(SkyblockerMod.NAMESPACE));
		}

		return stream;
	}
}
