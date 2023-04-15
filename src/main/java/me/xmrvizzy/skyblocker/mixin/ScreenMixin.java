package me.xmrvizzy.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(at = @At("HEAD"), method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", cancellable = true)
    public void skyblocker$renderTooltip(MatrixStack matrices, ItemStack itemStack, int x, int y, CallbackInfo ci) {
        Text stackName = itemStack.getName();
        String strName = stackName.getString();
        if (SkyblockerConfig.get().general.hideEmptyTooltips && strName.equals(" ")) {
            ci.cancel();
        }
    }

}
