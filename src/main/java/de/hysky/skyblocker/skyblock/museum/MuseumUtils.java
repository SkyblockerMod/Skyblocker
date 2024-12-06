package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class MuseumUtils {

	/**
	 * Calculates the total crafting cost for a set associated with a given ID.
	 *
	 * @param id the ID of the set for which the crafting cost is calculated
	 * @return the total crafting cost of the set
	 */
	protected static double getSetCraftCost(String id) {
		double cost = 0;
		for (Donation donation : MuseumItemCache.MUSEUM_DONATIONS) {
			if (donation.getId().equals(id)) {
				for (Pair<String, PriceData> piece : donation.getSet()){
					cost += ItemUtils.getCraftCost(piece.getLeft());
				}
			}
		}
		return cost;
	}

	/**
	 * Retrieves the display name for an item or a set.
	 * If the item is part of a set, it returns the set's name like "Divan's armor".
	 *
	 * @param id the ID of the item or set
	 * @param isSet true if the ID refers to a set, false if it refers to an individual item
	 * @return the display name of the item or set
	 */
	protected static Text getDisplayName(String id, boolean isSet) {
		if (isSet) {
			Style nameStyle = Style.EMPTY;
			String setName = MuseumItemCache.ARMOR_NAMES.get(id);
			if (setName != null) {
				Optional<Donation> donation = MuseumItemCache.MUSEUM_DONATIONS.stream().filter(d -> d.getId().equals(id)).findFirst();
				if (donation.isPresent()) {
					if (!donation.get().getSet().isEmpty()) {
						Text pieceName = getDisplayName(donation.get().getSet().getFirst().getLeft(), false);
						if (pieceName != null) {
							nameStyle = pieceName.getSiblings().getFirst().getStyle();
						}
					}
				}
				return Text.literal(setName).setStyle(nameStyle);
			}
		} else {
			ItemStack stack = ItemRepository.getItemStack(id);
			if (stack != null) {
				return stack.getName();
			}
		}
		return Text.literal(id);
	}

	/**
	 * Retrieves the set ID for a given piece ID.
	 *
	 * @param id the piece ID to search for
	 * @return the ID of the set that the piece belongs to, or null if not found
	 */
	protected static String getSetID(String id) {
		for (Donation donation : MuseumItemCache.MUSEUM_DONATIONS) {
			for (Pair<String, PriceData> set : donation.getSet()) {
				if (set.getLeft().equals(id)) {
					return donation.getId();
				}
			}
		}
		return null;
	}

	protected static List<String> getPiecesBySetID(String donationId) {
		return MuseumItemCache.MUSEUM_DONATIONS.stream()
				.filter(d -> d.getId().equals(donationId))
				.map(Donation::getSet)
				.flatMap(List::stream)
				.map(Pair::getLeft)
				.collect(Collectors.toList());
	}

	/**
	 * Formats an armor item ID into a readable name
	 */
	public static String formatArmorName(String id, boolean isEquipment) {
		String lowercaseKey = id.toLowerCase(Locale.ENGLISH);

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
	 * @return A formatted string (e.g., "10M", "5K", "1.2B").
	 */
	public static String formatPrice(double value) {
		NumberFormat NUMBER_FORMATTER_S = NumberFormat.getCompactNumberInstance(Locale.CANADA, NumberFormat.Style.SHORT);
		NUMBER_FORMATTER_S.setMaximumFractionDigits(1);
		return NUMBER_FORMATTER_S.format(value);
	}
}
