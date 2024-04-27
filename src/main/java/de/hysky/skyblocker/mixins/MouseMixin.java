package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.skyblock.garden.LowerSensitivity;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MouseMixin {

    @ModifyExpressionValue(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0))
    public Object skyblocker$gardenMouseLock(Object original) {
        if (LowerSensitivity.isSensitivityLowered())
            return -1 / 3d;
        else return original;

    }
}
