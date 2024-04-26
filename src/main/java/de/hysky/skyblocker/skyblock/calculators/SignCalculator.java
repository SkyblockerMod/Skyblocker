package de.hysky.skyblocker.skyblock.calculators;

import de.hysky.skyblocker.utils.Calculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;

public class SignCalculator {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final  DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    private static String lastInput;
    private static String input;
    private static Double output;

    public static void renderSign(DrawContext context, String[] messages){
        input = messages[0];


        //only update output if new input
        if (!input.equals(lastInput)) { //
            try {
                output = Calculator.calculate(input);
            } catch (Exception e){
                output = null;
            }
        }

        render(context);

        lastInput = input;
    }

    public static String getNewValue(Boolean isPrice) {
        if (output == null) {
            return "";
        }
        //price can except decimals and exponents
        if (isPrice) {
            return output.toString();
        }
        //amounts want an integer number so round
        return Long.toString(Math.round(output));
    }

    private static void render(DrawContext context) {
        Text text;
        if (output == null) {
            text = Text.translatable("text.autoconfig.skyblocker.option.general.enableSignCalculator.invalidEquation").formatted(Formatting.RED);
        }
        else {
            text = Text.literal(input +" = " + FORMATTER.format(output)).formatted(Formatting.GREEN);
        }

        context.drawCenteredTextWithShadow(CLIENT.textRenderer, text,context.getScaledWindowWidth() /2 , 55,0xFFFFFFFF);
    }
}
