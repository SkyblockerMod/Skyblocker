package de.hysky.skyblocker.skyblock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.awt.image.LookupTable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignCalculator {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+\\.?\\d*)([kmb]?)");
    private static final  DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    private static final HashMap<String, Integer> magnitudeValues = Util.make(new HashMap<>(), map -> {
        map.put("k", 1000);
        map.put("m", 1000000);
        map.put("b", 1000000000);
    });

    private static String input;
    private static Double output;

    public static void renderSign(DrawContext context, String[] messages){
        input = messages[0];

        calculateValue();

        render(context);
    }

    public static void calculateValue() {
        Matcher numberMatcher = NUMBER_PATTERN.matcher(input.toLowerCase());
        if (!numberMatcher.matches()) {
            output = null;
            return;
        }
        double number = Double.parseDouble(numberMatcher.group(1)); //
        String magnitude = numberMatcher.group(2);

        if (!magnitude.isEmpty()) {
            if (!magnitudeValues.containsKey(magnitude)) {//its invalid if its another letter
                output = null;
                return;
            }
            number *= magnitudeValues.get(magnitude);
        }

        output = number;

    }


    private static void render(DrawContext context) {
        Text text;
        if (output == null) {
            text = Text.literal("test").formatted(Formatting.RED);
        }
        else {
            text = Text.literal(input +" = " + FORMATTER.format(output)).formatted(Formatting.GREEN);
        }

        context.drawCenteredTextWithShadow(CLIENT.textRenderer, text,context.getScaledWindowWidth() /2 , 55,0xFFFFFFFF);
    }
}
