package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {

	public ContainerScreenMixin(ChestMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	// TODO(Ravel): remapper for com.llamalad7.mixinextras.expression.Expression is not implemented
// TODO(Ravel): remapper for com.llamalad7.mixinextras.expression.Expression is not implemented
    @Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;width:I")
	@Definition(id = "backgroundWidth", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;backgroundWidth:I")
	@Expression("(this.width - this.backgroundWidth) / ?")
	@ModifyExpressionValue(method = "renderBg", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int x(int ignored) {
		return leftPos;
	}

	// TODO(Ravel): remapper for com.llamalad7.mixinextras.expression.Expression is not implemented
// TODO(Ravel): remapper for com.llamalad7.mixinextras.expression.Expression is not implemented
    @Definition(id = "height", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;height:I")
	@Definition(id = "backgroundHeight", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;backgroundHeight:I")
	@Expression("(this.height - this.backgroundHeight) / ?")
	@ModifyExpressionValue(method = "renderBg", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int y(int ignored) {
		return topPos;
	}
}
