package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> {

	public GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Definition(id = "width", field = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;width:I")
	@Definition(id = "backgroundWidth", field = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;backgroundWidth:I")
	@Expression("(this.width - this.backgroundWidth) / ?")
	@ModifyExpressionValue(method = "drawBackground", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int x(int ignored) {
		return x;
	}

	@Definition(id = "height", field = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;height:I")
	@Definition(id = "backgroundHeight", field = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;backgroundHeight:I")
	@Expression("(this.height - this.backgroundHeight) / ?")
	@ModifyExpressionValue(method = "drawBackground", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int y(int ignored) {
		return y;
	}
}
