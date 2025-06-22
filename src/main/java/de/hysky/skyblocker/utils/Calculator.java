package de.hysky.skyblocker.utils;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    public enum TokenType {
        NUMBER, OPERATOR, L_PARENTHESIS, R_PARENTHESIS
    }

    public static class Token {
        public TokenType type;
        String value;
        int tokenLength;
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+\\.?\\d*)([sekmbt]?)");
    private static final Object2LongMap<String> MAGNITUDE_VALUES = Object2LongMaps.unmodifiable(new Object2LongOpenHashMap<>(Map.of(
            "s", 64L,
            "e", 160L,
            "k", 1_000L,
            "m", 1_000_000L,
            "b", 1_000_000_000L,
            "t", 1_000_000_000_000L
    )));

    private static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        input = input.replace(" ", "").toLowerCase().replace("x", "*");
        int i = 0;
        while (i < input.length()) {
            Token token = new Token();
            switch (input.charAt(i)) {
                case '+', '-', '*', '/', '^' -> {
                    token.type = TokenType.OPERATOR;
                    token.value = String.valueOf(input.charAt(i));
                    token.tokenLength = 1;

					// cant have double operators e.g. "5 ++ 2"
					if (!tokens.isEmpty() && tokens.getLast().type == TokenType.OPERATOR) {
						throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.duplicateOperatorError");
					}
                }

                case '(' -> {
                    token.type = TokenType.L_PARENTHESIS;
                    token.value = String.valueOf(input.charAt(i));
                    token.tokenLength = 1;
                    //add implicit multiplication when there is a number before brackets
                    if (!tokens.isEmpty()) {
                        TokenType lastType = tokens.getLast().type;
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
                        throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.invalidCharacterError");
                    }
                    int end = numberMatcher.end();
                    token.value = input.substring(i, i + end);
                    token.tokenLength = end;
                }
            }
            tokens.add(token);

            i += token.tokenLength;
        }

        return tokens;
    }

    /**
     * This is an implementation of the shunting yard algorithm to convert the equation to reverse polish notation
     *
     * @param tokens equation in infix notation order
     * @return equation in RPN order
     */
    private static List<Token> shunt(List<Token> tokens) {
        Deque<Token> operatorStack = new ArrayDeque<>();
        List<Token> outputQueue = new ArrayList<>();

        for (Token shuntingToken : tokens) {
            switch (shuntingToken.type) {
                case NUMBER -> outputQueue.add(shuntingToken);
                case OPERATOR -> {
                    int precedence = getPrecedence(shuntingToken.value);
                    while (!operatorStack.isEmpty()) {
                        Token leftToken = operatorStack.peek();
                        if (leftToken.type == TokenType.L_PARENTHESIS) {
                            break;
                        }
                        assert (leftToken.type == TokenType.OPERATOR);
                        int leftPrecedence = getPrecedence(leftToken.value);
                        if (leftPrecedence >= precedence) {
                            outputQueue.add(operatorStack.pop());
                            continue;
                        }
                        break;
                    }
                    operatorStack.push(shuntingToken);
                }
                case L_PARENTHESIS -> operatorStack.push(shuntingToken);
                case R_PARENTHESIS -> {
                    while (true) {
                        if (operatorStack.isEmpty()) {
                            throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.unbalancedParenthesisError");
                        }
                        Token leftToken = operatorStack.pop();
                        if (leftToken.type == TokenType.L_PARENTHESIS) {
                            break;
                        }
                        outputQueue.add(leftToken);
                    }
                }
            }
        }
        //empty the operator stack
        while (!operatorStack.isEmpty()) {
            Token leftToken = operatorStack.pop();
            if (leftToken.type == TokenType.L_PARENTHESIS) {
                //technically unbalanced left parenthesis error but just assume they are close after the equation and ignore them from here
                continue;
            }
            outputQueue.add(leftToken);
        }

        return outputQueue.stream().toList();
    }

    private static int getPrecedence(String operator) {
        switch (operator) {
            case "+", "-" -> {
                return 0;
            }
            case "*", "/" -> {
                return 1;
            }
			case "^" -> {
				return 2;
			}
            default -> throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.invalidOperatorError");
        }
    }

    /**
     * @param tokens list of Tokens in reverse polish notation
     * @return answer to equation
     */
    private static double evaluate(List<Token> tokens) {
        Deque<Double> values = new ArrayDeque<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER -> values.push(calculateValue(token.value));
                case OPERATOR -> {
                    Double right = values.pollFirst();
					Double left = values.pollFirst();
					if (left == null || right == null) {
						throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.missingValueError");
					}
                    switch (token.value) {
                        case "+" -> values.push(left + right);
                        case "-" -> values.push(left - right);
                        case "/" -> {
                            if (right == 0) {
                                throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.divisionByZeroError");
                            }
                            values.push(left / right);
                        }
                        case "*" -> values.push(left * right);
						case "^" -> values.push(Math.pow(left, right));
                    }
                }
                case L_PARENTHESIS, R_PARENTHESIS -> throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.badEquationNotation");
            }
        }
        if (values.isEmpty()) {
            throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.emptyEquationError");
        }
        return values.pop();
    }

    private static double calculateValue(String value) {
        Matcher numberMatcher = NUMBER_PATTERN.matcher(value.toLowerCase());
        if (!numberMatcher.matches()) {
            throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.invalidNumberError");
        }
        double number = Double.parseDouble(numberMatcher.group(1));
        String magnitude = numberMatcher.group(2);

        if (!magnitude.isEmpty()) {
            if (!MAGNITUDE_VALUES.containsKey(magnitude)) {//its invalid if its another letter
                throw new UnsupportedOperationException("skyblocker.config.uiAndVisuals.inputCalculator.invalidMagnitudeError");
            }
            number *= MAGNITUDE_VALUES.getLong(magnitude);
        }

        return number;
    }

    public static double calculate(String equation) {
        //custom bit for replacing purse with its value
        equation = equation.toLowerCase().replaceAll("p(urse)?", String.valueOf((long) Utils.getPurse()));
        return evaluate(shunt(lex(equation)));
    }
}
