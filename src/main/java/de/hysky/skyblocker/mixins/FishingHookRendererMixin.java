package de.hysky.skyblocker.mixins;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.projectile.FishingHook;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {

	@ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
	private boolean skyblocker$render(boolean original, @Local(argsOnly = true) FishingHook fishingBobberEntity) {
		//if rendered bobber is not the players and option to hide  others is enabled do not render the bobber
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().helpers.fishing.hideOtherPlayersRods
				? original && Objects.equals(Minecraft.getInstance().player, fishingBobberEntity.getPlayerOwner())
				: original;
	}
}
