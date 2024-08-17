package de.hysky.skyblocker.mixins;


import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.Objects;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin {
    @Shadow
    @Final
    private String[] messages;

    @Inject(method = "render", at = @At("HEAD"))
    private void skyblocker$render(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        //if the sign is being used to enter number send it to the sign calculator
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.enabled && Objects.equals(messages[1], "^^^^^^^^^^^^^^^")) {
            SignCalculator.renderCalculator(context, messages[0], context.getScaledWindowWidth() / 2, 55);
        }
    }

    @Inject(method = "finishEditing", at = @At("HEAD"))
    private void skyblocker$finishEditing(CallbackInfo ci) {
        //if the sign is being used to enter number get number from calculator for if maths has been done
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.enabled && Objects.equals(messages[1], "^^^^^^^^^^^^^^^")) {
            boolean isPrice = messages[2].contains("price");
            String value = SignCalculator.getNewValue(isPrice);
            if (value.length() >= 15) {
                value = value.substring(0, 15);
            }
            messages[0] = value;
        }
    }
}
