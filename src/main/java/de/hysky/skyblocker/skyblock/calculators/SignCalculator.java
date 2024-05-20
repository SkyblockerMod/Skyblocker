package de.hysky.skyblocker.skyblock.calculators;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Calculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;

public class SignCalculator {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final NumberFormat FORMATTER = NumberFormat.getInstance();

    private static String lastInput;
    private static double output;

    public static void renderCalculator(DrawContext context, String message, int renderX, int renderY) {
        if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.requiresEquals && !message.startsWith("=")) {
            output = -1;
            lastInput = message;
            return;
        }
        if (message.startsWith("=")) {
            message = message.substring(1);
        }
        //only update output if new input
        if (!message.equals(lastInput)) { //
            try {
                output = Calculator.calculate(message);
            } catch (Exception e) {
                output = -1;
            }
        }

        render(context, message, renderX, renderY);

        lastInput = message;
    }

    public static String getNewValue(Boolean isPrice) {
        if (output == -1) {
            //if mode is not activated or just invalid equation return what the user typed in
            return lastInput;
        }

        //price can except decimals and exponents
        if (isPrice) {
            return String.valueOf(output);
        }
        //amounts want an integer number so round
        return Long.toString(Math.round(output));
    }

    private static void render(DrawContext context, String input, int renderX, int renderY) {
        Text text;
        if (output == -1) {
            text = Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED);
        } else {
            text = Text.literal(input + " = " + FORMATTER.format(output)).formatted(Formatting.GREEN);
        }

        context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, renderX, renderY, 0xFFFFFFFF);
    }
}
