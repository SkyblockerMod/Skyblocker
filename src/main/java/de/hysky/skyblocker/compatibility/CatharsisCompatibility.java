package de.hysky.skyblocker.compatibility;

import java.util.function.Predicate;

public class CatharsisCompatibility {
	private static Predicate<String> hiddenModElementsProvider = _ -> false;

	public static void hiddenGuiElements(Predicate<String> provider) {
		hiddenModElementsProvider = provider;
	}

	public static boolean isGuiElementHidden(String element) {
		return hiddenModElementsProvider.test(element);
	}
}
