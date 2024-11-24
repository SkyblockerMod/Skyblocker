package de.hysky.skyblocker.skyblock.museum;

public class FormatingUtils {

	/**
	 * Formats an armor item ID into a readable name
	 */
	public static String formatArmorName(String id, boolean isEquipment) {
		String lowercaseKey = id.toLowerCase();

		// Step 2: Replace "_" with space
		String withSpaces = lowercaseKey.replace("_", " ");

		// Step 3: Capitalize the first letter of each word
		String[] words = withSpaces.split(" ");
		StringBuilder formattedName = new StringBuilder();
		for (String word : words) {
			// Uppercase first letter, lowercase the rest
			formattedName.append(Character.toUpperCase(word.charAt(0)))
					.append(word.substring(1))
					.append(" ");
		}

		if (isEquipment) {
			formattedName.append("Equipment");
		} else if (!lowercaseKey.contains("armor") &&
				!lowercaseKey.contains("outfit") &&
				!lowercaseKey.contains("suit") &&
				!lowercaseKey.contains("tuxedo")) {
			formattedName.append("Armor");
		}

		return formattedName.toString().trim();
	}

	/**
	 * Formats a double value into a shortened human-readable format.
	 *
	 * @param value The number to format.
	 * @return A formatted string (e.g., "10m", "5k", "1.2b").
	 */
	public static String formatPrice(double value) {
		String suffix = "";
		double divisor = 1;

		if (value >= 1_000_000_000) {
			suffix = "b";
			divisor = 1_000_000_000;
		} else if (value >= 1_000_000) {
			suffix = "m";
			divisor = 1_000_000;
		} else if (value >= 1_000) {
			suffix = "k";
			divisor = 1_000;
		}

		// Round the result first
		double result = value / divisor;
		// Prevent rounding up
		if (result >= 100) {
			result = (long) result; // Keep it as an integer
		} else {
			result = Math.floor(result * 10) / 10.0; // Round down to 1 decimal place
		}

		return (result == (long) result)
				? String.format("%d%s", (long) result, suffix)
				: String.format("%.1f%s", result, suffix);
	}
}
