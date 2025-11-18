package de.hysky.skyblocker.utils;

import com.demonwav.mcdev.annotations.Translatable;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.util.StringIdentifiable;

import java.io.Serial;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
	public enum TokenType {
		NUMBER, OPERATOR, FUNCTION, L_PARENTHESIS, R_PARENTHESIS
	}

	public abstract static class AbstractToken<T> {
		public final TokenType type;
		private final T value;

		public AbstractToken(TokenType type, T value) {
			this.type = type;
			this.value = value;
		}
	}

	public static class Token extends AbstractToken<String> {
		public Token(TokenType type, String value) {
			super(type, value);
		}
	}

	public static class OperatorToken extends AbstractToken<Operator> {
		public OperatorToken(Operator operator) {
			super(TokenType.OPERATOR, operator);
		}
	}

	public enum Operator implements StringIdentifiable {
		ADD("+"), SUB("-"), MULT("*"), DIV("/"), MOD("%"), POW("^", true);
		private static final java.util.function.Function<String, Operator> OPERATOR_MAP = StringIdentifiable.createMapper(Operator.values(), op -> op.op);
		private final String op;
		private final boolean rightAssociative;

		Operator(String op) {
			this(op, false);
		}

		Operator(String op, boolean rightAssociative) {
			this.op = op;
			this.rightAssociative = rightAssociative;
		}

		@Override
		public String asString() {
			return op;
		}
	}

	public static class FunctionToken extends AbstractToken<Function> {
		public FunctionToken(Function value) {
			super(TokenType.FUNCTION, value);
		}
	}

	public enum Function implements StringIdentifiable {
		SQRT("sqrt", val -> {
			if (val < 0) throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "sqrt");
			return Math.sqrt(val);
		}),
		LOG("log", val -> {
			if (val <= 0) throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "log");
			return Math.log10(val);
		}),
		LG("lg", val -> {
			if (val <= 0) throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "lg");
			return Math.log(val) / Math.log(2);
		}),
		LN("ln", val -> {
			if (val <= 0) throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "ln");
			return Math.log(val);
		}),
		FACTORIAL("factorial", val -> {
			if (val < 0 || val > 170) throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "factorial");
			double result = 1;
			for (int i = 2; i <= (int) val; i++) {
				result *= i;
			}
			return result;
		}),
		SIN("sin", val -> Math.sin(Math.toRadians(val))),
		COS("cos", val -> Math.cos(Math.toRadians(val))),
		TAN("tan", val -> Math.tan(Math.toRadians(val))),
		ASIN("asin", val -> {
			if (val < -1 || val > 1) {
				throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "asin");
			}
			return Math.toDegrees(Math.asin(val));
		}),
		ACOS("acos", val -> {
			if (val < -1 || val > 1) {
				throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidFunctionInputError", val, "acos");
			}
			return Math.toDegrees(Math.acos(val));
		}),
		ATAN("atan", val -> Math.toDegrees(Math.atan(val))),
		SINH("sinh", Math::sinh),
		COSH("cosh", Math::cosh),
		TANH("tanh", val -> {
			if (val > 20) {
				return 1.0;
			} else if (val < -20) {
				return -1.0;
			}
			return Math.tanh(val);
		}),
		ABS("abs", Math::abs),
		FLOOR("floor", Math::floor),
		CEIL("ceil", Math::ceil),
		ROUND("round", Math::round);

		private static final java.util.function.Function<String, Function> FUNCTION_MAP = StringIdentifiable.createMapper(Function.values(), func -> func.name);
		private final String name;
		private final CalculatorFunction function;

		Function(String name, CalculatorFunction function) {
			this.name = name;
			this.function = function;
		}

		@Override
		public String asString() {
			return name;
		}

		/**
		 * We do not extend {@link java.util.function.Function} because we need to throw exceptions.
		 */
		@FunctionalInterface
		private interface CalculatorFunction {
			double apply(double val) throws CalculatorException;
		}
	}

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+\\.?\\d*)([sekmbtq]?)");
	private static final Object2LongMap<String> MAGNITUDE_VALUES = Object2LongMaps.unmodifiable(new Object2LongOpenHashMap<>(Map.of(
			"s", 64L,
			"e", 160L,
			"k", 1_000L,
			"m", 1_000_000L,
			"b", 1_000_000_000L,
			"t", 1_000_000_000_000L,
			"q", 1_000_000_000_000_000L
	)));

	private static List<AbstractToken<?>> lex(String input) throws CalculatorException {
		List<AbstractToken<?>> tokens = new ArrayList<>();
		input = input.replace(" ", "").toLowerCase(Locale.ENGLISH).replace("x", "*");
		int i = 0;
		while (i < input.length()) {
			tokens.add(switch (input.charAt(i)) {
				case '+', '-', '*', '/', '%', '^' -> {
					String op = String.valueOf(input.charAt(i));
					Operator operator = Operator.OPERATOR_MAP.apply(op);
					if (operator == null) {
						throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidOperatorError", op);
					}
					// cant have double operators e.g. "5 ++ 2"
					if (!tokens.isEmpty() && tokens.getLast().type == TokenType.OPERATOR) {
						throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.duplicateOperatorError");
					}

					yield new OperatorToken(operator);
				}

				case '(' -> {
					//add implicit multiplication when there is a number before brackets
					if (!tokens.isEmpty()) {
						TokenType lastType = tokens.getLast().type;
						if (lastType == TokenType.R_PARENTHESIS || lastType == TokenType.NUMBER) {
							tokens.add(new OperatorToken(Operator.MULT));
						}
					}

					yield new Token(TokenType.L_PARENTHESIS, "(");
				}

				case ')' -> new Token(TokenType.R_PARENTHESIS, ")");

				case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
					Matcher numberMatcher = NUMBER_PATTERN.matcher(input.substring(i));
					if (!numberMatcher.find()) { //invalid value to lex
						throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidCharacterError", input.substring(i));
					}
					int end = numberMatcher.end();
					String number = input.substring(i, i + end);
					i += end - 1;
					yield new Token(TokenType.NUMBER, number);
				}

				default -> {
					String func = input.substring(i).split("[ (]", 2)[0];
					Function function = Function.FUNCTION_MAP.apply(func);
					if (function == null) {
						throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidOperatorError", func);
					}

					//add implicit multiplication when there is a number before functions
					if (!tokens.isEmpty()) {
						TokenType lastType = tokens.getLast().type;
						if (lastType == TokenType.R_PARENTHESIS || lastType == TokenType.NUMBER) {
							tokens.add(new OperatorToken(Operator.MULT));
						}
					}

					i += func.length() - 1;
					yield new FunctionToken(function);
				}
			});

			i++;
		}

		return tokens;
	}

	/**
	 * This is an implementation of the shunting yard algorithm to convert the equation to reverse polish notation
	 *
	 * @param tokens equation in infix notation order
	 * @return equation in RPN order
	 */
	private static List<AbstractToken<?>> shunt(List<AbstractToken<?>> tokens) throws CalculatorException {
		Deque<AbstractToken<?>> operatorStack = new ArrayDeque<>();
		List<AbstractToken<?>> outputQueue = new ArrayList<>();

		for (AbstractToken<?> shuntingToken : tokens) {
			switch (shuntingToken.type) {
				case NUMBER -> outputQueue.add(shuntingToken);
				case OPERATOR -> {
					Operator op = (Operator) shuntingToken.value;
					int precedence = getPrecedence(op);
					while (!operatorStack.isEmpty()) {
						AbstractToken<?> leftToken = operatorStack.peek();
						if (leftToken.type == TokenType.L_PARENTHESIS) {
							break;
						}
						assert (leftToken.type == TokenType.OPERATOR);
						int leftPrecedence = getPrecedence((Operator) leftToken.value);
						if (leftPrecedence > precedence || (leftPrecedence == precedence && !op.rightAssociative)) {
							outputQueue.add(operatorStack.pop());
							continue;
						}
						break;
					}
					operatorStack.push(shuntingToken);
				}
				case FUNCTION, L_PARENTHESIS -> operatorStack.push(shuntingToken);
				case R_PARENTHESIS -> {
					while (true) {
						if (operatorStack.isEmpty()) {
							throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.unbalancedParenthesisError");
						}
						AbstractToken<?> leftToken = operatorStack.pop();
						if (leftToken.type == TokenType.L_PARENTHESIS) {
							break;
						}
						outputQueue.add(leftToken);
					}
					if (!operatorStack.isEmpty() && operatorStack.peek().type == TokenType.FUNCTION) {
						outputQueue.add(operatorStack.pop());
					}
				}
			}
		}
		//empty the operator stack
		while (!operatorStack.isEmpty()) {
			AbstractToken<?> leftToken = operatorStack.pop();
			if (leftToken.type == TokenType.L_PARENTHESIS) {
				//technically unbalanced left parenthesis error but just assume they are close after the equation and ignore them from here
				continue;
			}
			outputQueue.add(leftToken);
		}

		return outputQueue.stream().toList();
	}

	private static int getPrecedence(Operator operator) {
		return switch (operator) {
			case ADD, SUB -> 0;
			case MULT, DIV, MOD -> 1;
			case POW -> 2;
		};
	}

	/**
	 * @param tokens list of Tokens in reverse polish notation
	 * @return answer to equation
	 */
	private static double evaluate(List<AbstractToken<?>> tokens) throws CalculatorException {
		Deque<Double> values = new ArrayDeque<>();
		for (AbstractToken<?> token : tokens) {
			switch (token.type) {
				case NUMBER -> values.push(calculateValue((String) token.value));
				case OPERATOR -> {
					if (values.size() < 2) {
						throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.missingValueError");
					}
					double right = values.pollFirst();
					@SuppressWarnings("DataFlowIssue") // We literally just checked the size
					double left = values.pollFirst();
					values.push(switch ((Operator) token.value) {
						case ADD -> left + right;
						case SUB -> left - right;
						case MULT -> left * right;
						case DIV -> {
							if (right == 0) {
								throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.divisionByZeroError", left);
							}
							yield (left / right);
						}
						case MOD -> {
							if (right == 0) {
								throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.moduloByZeroError", left);
							}
							yield (left % right);
						}
						case POW -> Math.pow(left, right);
					});
				}
				case FUNCTION -> {
					if (values.isEmpty()) {
						throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.missingValueError");
					}
					values.push(((Function) token.value).function.apply(values.pollFirst()));
				}
				case L_PARENTHESIS, R_PARENTHESIS -> throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.unbalancedParenthesisError");
			}
		}
		if (values.isEmpty()) {
			throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.emptyEquationError");
		}
		return values.pop();
	}

	private static double calculateValue(String value) throws CalculatorException {
		Matcher numberMatcher = NUMBER_PATTERN.matcher(value.toLowerCase(Locale.ENGLISH));
		if (!numberMatcher.matches()) {
			throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidNumberError", value.toLowerCase(Locale.ENGLISH));
		}
		double number = Double.parseDouble(numberMatcher.group(1));
		String magnitude = numberMatcher.group(2);

		if (!magnitude.isEmpty()) {
			if (!MAGNITUDE_VALUES.containsKey(magnitude)) { //it's invalid if it's another letter
				throw new CalculatorException("skyblocker.config.uiAndVisuals.inputCalculator.invalidMagnitudeError", magnitude);
			}
			number *= MAGNITUDE_VALUES.getLong(magnitude);
		}

		return number;
	}

	public static double calculate(String equation) throws CalculatorException {
		//custom bit for replacing purse with its value
		equation = equation.toLowerCase(Locale.ENGLISH).replaceAll("p(urse)?", String.valueOf((long) Utils.getPurse()));
		return evaluate(shunt(lex(equation)));
	}

	public static class CalculatorException extends Exception {
		@Serial
		private static final long serialVersionUID = -4480904461688998159L;
		public final Object[] args;

		public CalculatorException(@Translatable String message, Object... args) {
			super(message);
			this.args = args;
		}
	}
}
