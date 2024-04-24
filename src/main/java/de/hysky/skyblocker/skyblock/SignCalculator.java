package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Switch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignCalculator {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public enum TokenType {
        NUMBER, OPERATOR, L_PARENTHESIS, R_PARENTHESIS
    }
    public static class Token {
        public TokenType type;
        String value;
        int tokenLength;
    }
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+\\.?\\d*)([kmbs]?)");
    private static final HashMap<String, Integer> magnitudeValues = Util.make(new HashMap<>(), map -> {
        map.put("s", 64);
        map.put("k", 1000);
        map.put("m", 1000000);
        map.put("b", 1000000000);
    });




    private static final  DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    private static String lastInput;
    private static String input;
    private static Double output;

    public static void renderSign(DrawContext context, String[] messages){
        input = messages[0];


        //only update output if new input
        if (!input.equals(lastInput)) { //
            try {
                output = evaluate(shunt(lex(input)));
            } catch (Exception e){
                output = null; //todo log
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

    private static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        input = input.replace(" ", "").toLowerCase().replace("x","*");
        int i = 0;
        while (i < input.length()) {
            Token token = new Token();
            switch (input.charAt(i)) {
                case '+','-','*','/' -> {
                    token.type = TokenType.OPERATOR;
                    token.value = String.valueOf(input.charAt(i));
                    token.tokenLength = 1;
                }

                case '(' -> {
                    token.type = TokenType.L_PARENTHESIS;
                    token.value = String.valueOf(input.charAt(i));
                    token.tokenLength = 1;
                    //add implicit multiplication when there is a number before brackets
                    if (!tokens.isEmpty() ) {
                        TokenType lastType = tokens.get(tokens.size()-1).type;
                        if (lastType == TokenType.R_PARENTHESIS || lastType == TokenType.NUMBER) {
                            Token mutliplyToken = new Token();
                            mutliplyToken.type = TokenType.OPERATOR;
                            mutliplyToken.value = "*";
                            tokens.add(mutliplyToken);
                        }
                    }
                }

                case ')' -> {
                    token.type = TokenType.R_PARENTHESIS;
                    token.value = String.valueOf(input.charAt(i));
                    token.tokenLength = 1;
                }

                default -> {
                    token.type = TokenType.NUMBER;
                    Matcher numberMatcher = NUMBER_PATTERN.matcher(input.substring(i));
                    if (!numberMatcher.find()) {//invalid value to lex
                        throw new UnsupportedOperationException();
                    }
                    int end =  numberMatcher.end();
                    token.value = input.substring(i,i + end);
                    token.tokenLength = end;
                }
            }
            tokens.add(token);

            i += token.tokenLength;
        }

        return tokens;
    }

    private static List<Token> shunt(List<Token> tokens) {
        // This is an implementation of the shunting yard algorithm
        // Converts equation to use reverse polish notation

        Deque<Token> operatorStack = new ArrayDeque<>();
        List<Token> outputQueue = new ArrayList<>();

        for (Token shuntingToken : tokens)
            switch (shuntingToken.type) {
                case NUMBER -> {
                    outputQueue.add(shuntingToken);
                }
                case OPERATOR -> {
                    int precedence = getPrecedence(shuntingToken.value);
                    while (!operatorStack.isEmpty()) {
                        Token leftToken = operatorStack.peek();
                        if (leftToken.type == TokenType.L_PARENTHESIS) {
                            break;
                        }
                        assert (leftToken.type == TokenType.OPERATOR); //todo why is this here
                        int leftPrecedence = getPrecedence(leftToken.value);
                        if (leftPrecedence >= precedence) {
                            outputQueue.add(operatorStack.pop());
                            continue;
                        }
                        break;
                    }
                    operatorStack.push(shuntingToken);
                }
                case L_PARENTHESIS -> {
                    operatorStack.push(shuntingToken);
                }
                case  R_PARENTHESIS -> {
                    while (true) {
                        if (operatorStack.isEmpty()) {
                            throw new UnsupportedOperationException("Unbalanced left parenthesis");
                        }
                        Token leftToken = operatorStack.pop();
                        if (leftToken.type == TokenType.L_PARENTHESIS) {
                            break;
                        }
                        outputQueue.add(leftToken);
                    }
                }
            }
        //empty the operator stack
        while (!operatorStack.isEmpty()) {
            Token leftToken = operatorStack.pop();
            if (leftToken.type == TokenType.L_PARENTHESIS) {
                throw new UnsupportedOperationException("Unbalanced left parenthesis");
            }
            outputQueue.add(leftToken);
        }

        return outputQueue.stream().toList();
    }
    private static int getPrecedence(String operator) {
        switch (operator) {
            case "+","-" -> {
                return 0;
            }
            case "*","/" -> {
                return 1;
            }
            default -> {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     *
     * @param tokens list of Tokens in reverse polish notation
     * @return answer to equation
     */
    private static double evaluate(List<Token> tokens) {
        Deque<Double> values = new ArrayDeque<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER -> {
                    values.push(calculateValue(token.value));
                }
                case OPERATOR -> {
                    double right = values.pop();
                    double left = values.pop();
                    switch (token.value) {
                        case "+" -> {
                            values.push(left + right);
                        }
                        case "-" -> {
                            values.push(left - right);
                        }
                        case "/" -> {
                            values.push(left / right);
                        }
                        case "*" -> {
                            values.push(left * right);
                        }
                    }
                }
                case L_PARENTHESIS, R_PARENTHESIS -> {
                    throw new UnsupportedOperationException("equation is not in RPN");
                }
            }
        }
        return values.pop();
    }

    private static double calculateValue(String value) {
        Matcher numberMatcher = NUMBER_PATTERN.matcher(value.toLowerCase());
        if (!numberMatcher.matches()) {
            throw new UnsupportedOperationException();
        }
        double number = Double.parseDouble(numberMatcher.group(1));
        String magnitude = numberMatcher.group(2);

        if (!magnitude.isEmpty()) {
            if (!magnitudeValues.containsKey(magnitude)) {//its invalid if its another letter
                throw new UnsupportedOperationException();
            }
            number *= magnitudeValues.get(magnitude);
        }

        return number;
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
