package de.hysky.skyblocker.mixins;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.injected.SkyblockerGraphicsExtractor;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.text.GridComponentManager;
import de.hysky.skyblocker.utils.render.text.GridTooltipComponent;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements SkyblockerGraphicsExtractor {
	@Unique
	private final List<Renderable> deferredElements = new ArrayList<>();

	@ModifyExpressionValue(method = "itemCooldown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;getCooldownPercent(Lnet/minecraft/world/item/ItemStack;F)F"))
	private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(name = "itemStack") ItemStack stack) {
		return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
	}

	@Inject(method = "tooltip", at = @At("HEAD"))
	private void initializeGrids(Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner, @Nullable Identifier style, CallbackInfo ci) {
		// null initially to not create instances needlessly, might be premature optimization but oh well
		GridComponentManager manager = null;
		for (ClientTooltipComponent line : lines) {
			if (line instanceof GridTooltipComponent component) {
				if (manager == null) manager = new GridComponentManager();
				component.setManager(manager);
			}
		}
	}

	@Inject(method = "extractDeferredElements", at = @At("TAIL"))
	private void extractDeferredElements(int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (deferredElements.isEmpty()) return;
		for (Renderable renderable : deferredElements) {
			renderable.extractRenderState((GuiGraphicsExtractor) (Object) this, mouseX, mouseY, a);
		}
		deferredElements.clear();
	}

	@Override
	public void skb$addDeferredElement(Renderable renderable) {
		deferredElements.add(renderable);
	}
}
