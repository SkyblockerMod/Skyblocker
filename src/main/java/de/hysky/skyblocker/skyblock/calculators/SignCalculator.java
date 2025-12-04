package de.hysky.skyblocker.skyblock.calculators;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Calculator;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.VisibleForTesting;

public class SignCalculator {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static String lastInput;
	private static double output;
	private static Text error;

	public static void renderCalculator(DrawContext context, String message, int renderX, int renderY) {
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.requiresEquals && !message.startsWith("=")) {
			output = -1;
			error = null;
			lastInput = message;
			return;
		}
		if (message.startsWith("=")) {
			message = message.substring(1);
		}
		calculate(message);

		render(context, message, renderX, renderY);
	}

	@VisibleForTesting
	public static void calculate(String message) {
		//only update output if new input
		if (!message.equals(lastInput)) {
			lastInput = message;
			try {
				output = Calculator.calculate(message);
				error = null;
			} catch (Calculator.CalculatorException e) {
				output = -1;
				error = Text.translatable(e.getMessage(), e.args).formatted(Formatting.RED);
			}
		}
	}

	public static String getNewValue(boolean isPrice) {
		if (output == -1) {
			//if mode is not activated or just invalid equation return what the user typed in
			return lastInput;
		}

		//price can except decimals and exponents
		if (isPrice) {
			return String.valueOf(Math.round(output * 100d) / 100d);
		}
		//amounts want an integer number so round
		return Long.toString(Math.round(output));
	}

	private static void render(DrawContext context, String input, int renderX, int renderY) {
		Text text;
		if (output == -1) {
			text = error != null ? error : Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED);
		} else {
			text = Text.literal(input + " = " + Formatters.DOUBLE_NUMBERS.format(output)).formatted(Formatting.GREEN);
		}

		context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, renderX, renderY, 0xFFFFFFFF);
	}
}
