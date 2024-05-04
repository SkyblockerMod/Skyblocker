package de.hysky.skyblocker.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;

@Mixin(SocialInteractionsPlayerListWidget.class)
public class SocialInteractionsPlayerListWidgetMixin {

	@WrapOperation(method = "setPlayers", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
	private Object skyblocker$hideInvalidPlayers(Map<Object, Object> map, Object uuid, Object entry, Operation<Object> operation) {
		if (Utils.isOnSkyblock() && !((SocialInteractionsPlayerListEntry) entry).getName().matches("[A-Za-z0-9_]+")) return null;

		return operation.call(map, uuid, entry);
	}
}
