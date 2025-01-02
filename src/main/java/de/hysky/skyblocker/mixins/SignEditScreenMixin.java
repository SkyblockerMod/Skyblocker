package de.hysky.skyblocker.mixins;


import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import de.hysky.skyblocker.skyblock.speedPreset.SpeedPresets;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

    @Shadow
    @Final
    private String[] messages;

	protected SignEditScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "render", at = @At("HEAD"))
    private void skyblocker$render(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
		if (Utils.isOnSkyblock()) {
			var config = SkyblockerConfigManager.get();
			if (messages[1].equals("^^^^^^") && config.general.speedPresets.enableSpeedPresets) {
				var presets = SpeedPresets.getInstance();
				if (presets.hasPreset(messages[0])) {
					context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(String.format("%s » %d", messages[0], presets.getPreset(messages[0]))).formatted(Formatting.GREEN),
							context.getScaledWindowWidth() / 2, 55, 0xFFFFFFFF);
				}
			}
			//if the sign is being used to enter number send it to the sign calculator
			if (messages[1].equals("^^^^^^^^^^^^^^^") && config.uiAndVisuals.inputCalculator.enabled) {
				SignCalculator.renderCalculator(context, messages[0], context.getScaledWindowWidth() / 2, 55);
			}
		}
    }

    @Inject(method = "finishEditing", at = @At("HEAD"))
    private void skyblocker$finishEditing(CallbackInfo ci) {
		var config = SkyblockerConfigManager.get();
        if (Utils.isOnSkyblock()) {
			//if the sign is being used to enter the speed cap, retrieve the value from speed presets.
			if (messages[1].equals("^^^^^^") && config.general.speedPresets.enableSpeedPresets) {
				var presets = SpeedPresets.getInstance();
				if (presets.hasPreset(messages[0])) {
					messages[0] = String.valueOf(presets.getPreset(messages[0]));
				}
			}
			//if the sign is being used to enter number get number from calculator for if maths has been done
			if (messages[1].equals("^^^^^^^^^^^^^^^") && config.uiAndVisuals.inputCalculator.enabled) {
				boolean isPrice = messages[2].contains("price");
				String value = SignCalculator.getNewValue(isPrice);
				if (value.length() >= 15) {
					value = value.substring(0, 15);
				}
				messages[0] = value;
			}
        }
    }
}
