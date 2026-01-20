package de.hysky.skyblocker.mixins;

import java.util.Map;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.utils.Utils;

@Mixin(SocialInteractionsPlayerList.class)
public class SocialInteractionsPlayerListMixin {

	@WrapWithCondition(method = "addOnlinePlayers", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private boolean skyblocker$hideInvalidPlayers(Map<Object, Object> map, Object uuid, Object entry) {
		return !(Utils.isOnSkyblock() && !((PlayerEntry) entry).getPlayerName().matches("[A-Za-z0-9_]+"));
	}
}
