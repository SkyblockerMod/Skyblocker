package de.hysky.skyblocker.skyblock.calculators;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignTimeCalculator
{
	public enum TimeType {
		HOURS, MINUTES
	}
	
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Pattern TIME_CODE_PATTERN = Pattern.compile("(\\d+\\.\\d+|\\d+)(\\D+)?");
	private static final int SECONDS_PER_DAY = 86400;
	private static final short SECONDS_PER_HOUR = 3600;
	private static final byte SECONDS_PER_MINUTE = 60;
	
	private static @Nullable String lastInput;
	private static long seconds;
	private static @Nullable Component error = null;
	
	public static void renderTime(GuiGraphics context, String message, TimeType type, int renderX, int renderY) {
		calculate(message, type);
		render(context, renderX, renderY);
	}
	
	@VisibleForTesting
	public static void calculate(String message, TimeType type) {
		//only update if new input
		if (message.equals(lastInput)) return;
		lastInput = message;
		
		if (message.isBlank()) {
			seconds = -1;
			error = Component.translatable("skyblocker.config.uiAndVisuals.timeInputCalculator.emptyInputError").withStyle(ChatFormatting.RED);
			return;
		}
		
		long newSeconds = 0;
		for (String segment : message.trim().split(" +")) {
			Matcher matcher = TIME_CODE_PATTERN.matcher(segment);
			if (matcher.matches()) {
				String numberString = matcher.group(1);
				if (numberString.startsWith("-")) {
					seconds = -1;
					error = Component.translatable("skyblocker.config.uiAndVisuals.timeInputCalculator.invalidNumberError", numberString).withStyle(ChatFormatting.RED);
					return;
				}
				double number;
				try {
					number = Double.parseDouble(numberString);
				} catch (NumberFormatException e) {
					seconds = -1;
					error = Component.translatable("skyblocker.config.uiAndVisuals.timeInputCalculator.invalidNumberError", numberString).withStyle(ChatFormatting.RED);
					return;
				}
				
				String timeUnitString = matcher.group(2);
				if (timeUnitString == null || timeUnitString.isEmpty()) {
					newSeconds += switch (type) {
						case HOURS -> Math.round(number * SECONDS_PER_HOUR);
						case MINUTES -> Math.round(number * SECONDS_PER_MINUTE);
					};
				}
				//mimics how Hypixel parses numbers, except also allowing plurals
				else if ("days".startsWith(timeUnitString.toLowerCase())) {
					newSeconds += Math.round(number * SECONDS_PER_DAY);
				} else if ("hours".startsWith(timeUnitString.toLowerCase())) {
					newSeconds += Math.round(number * SECONDS_PER_HOUR);
				} else if ("minutes".startsWith(timeUnitString.toLowerCase())) {
					newSeconds += Math.round(number * SECONDS_PER_MINUTE);
				} else if ("seconds".startsWith(timeUnitString.toLowerCase())) {
					//note/fun fact: for some reason Hypixel only allows up to "secon", not "second"
					newSeconds += Math.round(number);
				} else {
					seconds = -1;
					error = Component.translatable("skyblocker.config.uiAndVisuals.timeInputCalculator.invalidTimeUnit", timeUnitString).withStyle(ChatFormatting.RED);
					return;
				}
			} else {
				seconds = -1;
				error = Component.translatable("skyblocker.config.uiAndVisuals.timeInputCalculator.invalidInputSpecific", segment).withStyle(ChatFormatting.RED);
				return;
			}
		}
		
		seconds = newSeconds;
		error = null;
	}

	public static @Nullable String getNewValue() {
		if (seconds == -1) {
			//if calculator is not activated or just invalid input return what the user typed in
			return lastInput;
		}
		
		//we can just return the total seconds, Hypixel converts it for us :)
		return seconds + "s";
	}

	private static void render(GuiGraphics context, int renderX, int renderY) {
		Component text;
		if (seconds == -1) {
			text = error != null ? error : Component.translatable("skyblocker.config.uiAndVisuals.timeInputCalculator.invalidInput").withStyle(ChatFormatting.RED);
		} else {
			long remainingSeconds = seconds;
			MutableComponent timeText = Component.empty();
			remainingSeconds = appendTimeComponent(timeText, remainingSeconds, SECONDS_PER_DAY, "day");
			remainingSeconds = appendTimeComponent(timeText, remainingSeconds, SECONDS_PER_HOUR, "hour");
			remainingSeconds = appendTimeComponent(timeText, remainingSeconds, SECONDS_PER_MINUTE, "minute");
			appendTimeComponent(timeText, remainingSeconds, 1, "second");
			text = timeText.withStyle(ChatFormatting.GREEN);
		}

		context.drawCenteredString(CLIENT.font, text, renderX, renderY, 0xFFFFFFFF);
	}
	
	private static long appendTimeComponent(MutableComponent component, long seconds, int secondsPerUnit, String translationKeyUnit)
	{
		long units = seconds / secondsPerUnit; //long division -> cuts off decimal places automatically
		if (units > 0) {
			seconds -= units * secondsPerUnit;
			if (!component.equals(CommonComponents.EMPTY)) {
				component.append(" ");
			}
			component.append(Component.translatable(
				"skyblocker.config.uiAndVisuals.timeInputCalculator.unit." + translationKeyUnit + (units == 1 ? "" : "s"),
				units
			));
		}
		return seconds;
	}
}
