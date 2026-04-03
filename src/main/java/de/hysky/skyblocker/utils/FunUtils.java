package de.hysky.skyblocker.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class FunUtils {
	private static final boolean FUN_DISABLED = Boolean.parseBoolean(System.getProperty("skyblocker.iAmABoringPersonAndHateFun", "false"));
	public static final Random RANDOM = new Random();

	private static final String OBFUSCATED_NAME = "Corruptedblocker";
	public static final List<String> FUN_NAMES = List.of(
			"Skibidiblocker",
			"Skiblocker",
			OBFUSCATED_NAME,
			"AAAAAAAAblocker",
			"camelCaseBlocker"
	);

	public static Component getRandomFunName() {
		return createUnpleasantGradient(FUN_NAMES.get(RANDOM.nextInt(FUN_NAMES.size())));
	}

	public static boolean shouldEnableFun() {
		if (FUN_DISABLED) return false;
		LocalDate date = LocalDate.now();
		return date.getMonthValue() == 4 && date.getDayOfMonth() == 1;
	}

	public static boolean shouldEnableChristmasFun() {
		if (FUN_DISABLED) return false;
		LocalDate date = LocalDate.now();
		return date.getMonthValue() == 12 && (date.getDayOfMonth() == 24 || date.getDayOfMonth() == 25);
	}

	public static boolean shouldEnableSpookyFun() {
		if (FUN_DISABLED) return false;
		LocalDate date = LocalDate.now();
		return date.getMonthValue() == 10 && date.getDayOfMonth() == 31;
	}

	public static Component createUnpleasantGradient(String string) {
		boolean obfuscated = string.equals(OBFUSCATED_NAME);
		MutableComponent component = Component.empty();
		int halfSize = string.length() / 2;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			int startColor = i >= halfSize ? 0xFD2EF6 : 0x22D71D;
			int endColor = i >= halfSize ? 0x9B5300 : 0xFD2EF6;
			float progress = Math.clamp((i % halfSize) / (float) halfSize, 0, 1); // lazy fix for it being greater than 1 occasionally
			// don't use oklab to make the gradient even worse fire emoji
			component.append(Component.literal(String.valueOf(c)).withColor(ARGB.srgbLerp(progress, startColor, endColor)).withStyle(style -> style.withObfuscated(obfuscated)));
		}
		return component;
	}
}
