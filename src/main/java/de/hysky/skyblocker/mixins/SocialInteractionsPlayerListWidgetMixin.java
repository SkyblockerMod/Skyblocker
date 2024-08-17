package de.hysky.skyblocker.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;

@Mixin(SocialInteractionsPlayerListWidget.class)
public class SocialInteractionsPlayerListWidgetMixin {

	@WrapWithCondition(method = "setPlayers", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
	private boolean skyblocker$hideInvalidPlayers(Map<Object, Object> map, Object uuid, Object entry) {
		return !(Utils.isOnSkyblock() && !((SocialInteractionsPlayerListEntry) entry).getName().matches("[A-Za-z0-9_]+"));
	}
}
