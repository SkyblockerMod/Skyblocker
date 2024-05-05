package de.hysky.skyblocker.skyblock.calculators;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Calculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;

public class SignCalculator {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    private static String lastInput;
    private static Double output;

    public static void renderCalculator(DrawContext context, String message, int renderX, int renderY) {
        if (SkyblockerConfigManager.get().general.inputCalculator.requiresEquals) {
            if (message.startsWith("=")) {
                message = message.substring(1);
            }
            else {
                output = null;
                lastInput = message;
                return;
            }
        }
        //only update output if new input
        if (!message.equals(lastInput)) { //
            try {
                output = Calculator.calculate(message);
            } catch (Exception e) {
                output = null;
            }
        }

        render(context, message, renderX, renderY);

        lastInput = message;
    }

    public static String getNewValue(Boolean isPrice) {
        if (output == null) {
            //if mode is not activated or just invalid equation return what the user typed in
            return lastInput;
        }

        //price can except decimals and exponents
        if (isPrice) {
            return output.toString();
        }
        //amounts want an integer number so round
        return Long.toString(Math.round(output));
    }

    private static void render(DrawContext context, String input, int renderX, int renderY) {
        Text text;
        if (output == null) {
            text = Text.translatable("text.autoconfig.skyblocker.option.general.inputCalculator.invalidEquation").formatted(Formatting.RED);
        } else {
            text = Text.literal(input + " = " + FORMATTER.format(output)).formatted(Formatting.GREEN);
        }

        context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, renderX, renderY, 0xFFFFFFFF);
    }
}
