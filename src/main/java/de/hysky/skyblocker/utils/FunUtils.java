package de.hysky.skyblocker.utils;

import java.time.LocalDate;

public class FunUtils {
	private static final boolean FUN_DISABLED = Boolean.parseBoolean(System.getProperty("skyblocker.iAmABoringPersonAndHateFun", "false"));

	public static boolean shouldEnableFun() {
		if (FUN_DISABLED) return false;
		LocalDate date = LocalDate.now();
		return date.getMonthValue() == 4 && date.getDayOfMonth() == 1;
	}
}
