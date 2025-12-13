package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {

	public ContainerScreenMixin(ChestMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;width:I")
	@Definition(id = "imageWidth", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;imageWidth:I")
	@Expression("(this.width - this.imageWidth) / ?")
	@ModifyExpressionValue(method = "renderBg", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int x(int ignored) {
		return leftPos;
	}

	@Definition(id = "height", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;height:I")
	@Definition(id = "imageHeight", field = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;imageHeight:I")
	@Expression("(this.height - this.imageHeight) / ?")
	@ModifyExpressionValue(method = "renderBg", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int y(int ignored) {
		return topPos;
	}
}
