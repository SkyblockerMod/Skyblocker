package de.hysky.skyblocker.mixins.repoparser;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.moulberry.repo.NEUItems;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.util.NEUId;

@Mixin(value = NEUItems.class, remap = false)
public class NEUItemsMixin {
	@Shadow
	Map<@NEUId String, NEUItem> items;

	@Inject(method = "reload", at = @At("HEAD"))
	private void skyblocker$createLocalMap(CallbackInfo ci, @Share("itemsMap") LocalRef<Map<String, NEUItem>> itemsMap) {
		itemsMap.set(new HashMap<>());
	}

	@ModifyReceiver(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private Map<String, NEUItem> skyblocker$putItemsInLocalMap(Map<String, NEUItem> original, Object key, Object value, @Share("itemsMap") LocalRef<Map<String, NEUItem>> itemsMap) {
		return itemsMap.get();
	}

	@Inject(method = "reload", at = @At("TAIL"))
	private void skyblocker$assignMap(CallbackInfo ci, @Share("itemsMap") LocalRef<Map<String, NEUItem>> itemsMap) {
		this.items = itemsMap.get();
	}
}
