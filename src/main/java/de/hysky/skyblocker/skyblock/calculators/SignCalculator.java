package de.hysky.skyblocker.skyblock.calculators;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Calculator;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.VisibleForTesting;

public class SignCalculator {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static String lastInput;
	private static double output;
	private static Component error;

	public static void renderCalculator(GuiGraphics context, String message, int renderX, int renderY) {
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
				error = Component.translatable(e.getMessage(), e.args).withStyle(ChatFormatting.RED);
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

	private static void render(GuiGraphics context, String input, int renderX, int renderY) {
		Component text;
		if (output == -1) {
			text = error != null ? error : Component.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").withStyle(ChatFormatting.RED);
		} else {
			text = Component.literal(input + " = " + Formatters.DOUBLE_NUMBERS.format(output)).withStyle(ChatFormatting.GREEN);
		}

		context.drawCenteredString(CLIENT.font, text, renderX, renderY, 0xFFFFFFFF);
	}
}
