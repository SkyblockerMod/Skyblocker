package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.text.GridComponentManager;
import de.hysky.skyblocker.utils.render.text.GridTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
	@ModifyExpressionValue(method = "itemCooldown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;getCooldownPercent(Lnet/minecraft/world/item/ItemStack;F)F"))
	private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(name = "itemStack") ItemStack stack) {
		return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
	}

	@Inject(method = "setTooltipForNextFrameInternal", at = @At("HEAD"))
	private void initializeGrids(Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner, @Nullable Identifier style, boolean replaceExisting, CallbackInfo ci) {
		GridComponentManager manager = null;
		for (ClientTooltipComponent line : lines) {
			if (line instanceof GridTooltipComponent component) {
				if (manager == null) manager = new GridComponentManager();
				component.setManager(manager);
			}
		}
	}
}
