package de.hysky.skyblocker.mixin;


import de.hysky.skyblocker.skyblock.SignCalculator;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin{
    @Shadow
    @Final
    private String[] messages;

    @Inject(method = "render", at = @At("HEAD"))
    private void skyblocker$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        //if the sign is being used to enter number send it to the sign calculator
        if (Objects.equals(messages[1], "^^^^^^^^^^^^^^^")) {
            SignCalculator.renderSign(context, messages);
        }
    }

    @Inject(method = "finishEditing", at = @At("HEAD"))
    private void skyblocker$finishEditing(CallbackInfo ci) {
        //if the sign is being used to enter number get number from calculator for if maths has been done
        if (Objects.equals(messages[1], "^^^^^^^^^^^^^^^")) {
            messages[0] = SignCalculator.getNewValue();
        }

    }

}
