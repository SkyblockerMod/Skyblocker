package de.hysky.skyblocker.mixins.jgit;

import java.util.Map;

import org.eclipse.jgit.transport.UrlConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(UrlConfig.class)
public class UrlConfigMixin {

	@WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object skyblocker$ignoreUrlRedirects(Map<String, String> map, Object key, Object value, Operation<Object> operation) {
		return null;
	}
}
