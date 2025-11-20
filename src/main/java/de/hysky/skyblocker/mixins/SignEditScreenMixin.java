package de.hysky.skyblocker.mixins;


import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.bazaar.BazaarQuickQuantities;
import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import de.hysky.skyblocker.skyblock.speedpreset.SpeedPresets;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

    @Shadow
    @Final
    private String[] messages;

	@Shadow
	public abstract void close();

	protected SignEditScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void skyblocker$init(CallbackInfo ci) {
		if (Utils.isOnSkyblock()) {
			var config = SkyblockerConfigManager.get();
			if (isInputSign() && messages[3].equals("to order") && config.uiAndVisuals.bazaarQuickQuantities.enabled) {
				ButtonWidget[] buttons = BazaarQuickQuantities.getButtons(this.width, messages);
				for (ButtonWidget button : buttons) if (button != null) addDrawableChild(button);
			}
		}
	}

	@Inject(method = "render", at = @At("HEAD"))
    private void skyblocker$render(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
		if (Utils.isOnSkyblock()) {
			var config = SkyblockerConfigManager.get();
			if (isSpeedInputSign() && config.general.speedPresets.enableSpeedPresets) {
				var presets = SpeedPresets.getInstance();
				if (presets.hasPreset(messages[0])) {
					context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(String.format("%s » %d", messages[0], presets.getPreset(messages[0]))).formatted(Formatting.GREEN),
							context.getScaledWindowWidth() / 2, 55, 0xFFFFFFFF);
				}
			}
			//if the sign is being used to enter number send it to the sign calculator
			else if (isInputSign() && config.uiAndVisuals.inputCalculator.enabled) {
				SignCalculator.renderCalculator(context, messages[0], context.getScaledWindowWidth() / 2, 55);
			}
		}
    }

	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void skyblocker$keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.closeSignsWithEnter
				&& Utils.isOnSkyblock() && isInputSign()
				&& (input.isEnter())) this.close();
	}

    @Inject(method = "finishEditing", at = @At("HEAD"))
    private void skyblocker$finishEditing(CallbackInfo ci) {
		var config = SkyblockerConfigManager.get();
        if (Utils.isOnSkyblock()) {
			//if the sign is being used to enter the speed cap, retrieve the value from speed presets.
			if (isSpeedInputSign() && config.general.speedPresets.enableSpeedPresets) {
				var presets = SpeedPresets.getInstance();
				if (presets.hasPreset(messages[0])) {
					messages[0] = String.valueOf(presets.getPreset(messages[0]));
				}
			}
			//if the sign is being used to enter number get number from calculator for if maths has been done
			else if (isInputSign() && config.uiAndVisuals.inputCalculator.enabled) {
				boolean isPrice = messages[2].contains("price");
				String value = SignCalculator.getNewValue(isPrice);
				if (value.length() >= 15) {
					value = value.substring(0, 15);
				}
				messages[0] = value;
			}
        }
    }

	@Unique
	private static final String SPEED_INPUT_MARKER = "speed cap!";
	@Unique
	private static final String INPUT_SIGN_MARKER = "^^^^^^^^^^^^^^^";
	/** This is used for some things like the super craft amount input */
	@Unique
	private static final String ALT_INPUT_SIGN_MARKER = "^^^^^^";
	@Unique
	private static final String BAZAAR_FLIP_MARKER = "^^Flipping^^";

	@Unique
	private boolean isSpeedInputSign() {
		return messages[3].equals(SPEED_INPUT_MARKER);
	}

	/**
	 * Used to exclude search signs with {@link SignEditScreenMixin#INPUT_SIGN_MARKER}.
	 * <br> Works for /recipes & /shards signs
	 */
	@Unique
	private boolean isInputSearchSign() {
		return messages[2].endsWith("your") || messages[2].endsWith("query");
	}

	/**
	 * Used to exclude search signs with {@link SignEditScreenMixin#ALT_INPUT_SIGN_MARKER}
	 * <br> Works for the /bestiary sign
	 */
	@Unique
	private boolean isAltInputSearchSign() {
		return messages[2].endsWith("your");
	}

	@Unique
	private boolean isInputSign() {
		return messages[1].equals(INPUT_SIGN_MARKER) && !isInputSearchSign() || messages[1].equals(ALT_INPUT_SIGN_MARKER) && !isAltInputSearchSign() || messages[1].equals(BAZAAR_FLIP_MARKER);
	}
}
