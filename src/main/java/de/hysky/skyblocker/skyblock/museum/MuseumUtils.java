package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MuseumUtils {
	private static final Set<String> EQUIPMENT_TYPES = Set.of("BELT", "GLOVES", "CLOAK", "GAUNTLET", "NECKLACE", "BRACELET", "HAT", "LOCKET", "VINE", "GRIPPERS");

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
				for (ObjectObjectMutablePair<String, PriceData> piece : donation.getSet()) {
					cost += ItemUtils.getCraftCost(piece.left());
				}
			}
		}
		return cost;
	}

	/**
	 * Retrieves the display name for an item or a set.
	 * If the item is part of a set, it returns the set's name like "Divan's armor".
	 *
	 * @param id    the ID of the item or set
	 * @param isSet true if the ID refers to a set, false if it refers to an individual item
	 * @return the display name of the item or set
	 */
	protected static Component getDisplayName(String id, boolean isSet) {
		if (isSet) {
			Style nameStyle = Style.EMPTY;
			String setName = MuseumItemCache.ARMOR_NAMES.get(id);
			if (setName != null) {
				for (Donation donation : MuseumItemCache.MUSEUM_DONATIONS) {
					if (donation.getId().equals(id) && !donation.getSet().isEmpty()) {
						Component pieceName = getDisplayName(donation.getSet().getFirst().left(), false);
						if (pieceName != null) {
							List<Component> siblings = pieceName.getSiblings();
							nameStyle = siblings.isEmpty() ? Style.EMPTY : siblings.getFirst().getStyle();
						}
						break;
					}
				}
				return Component.literal(setName).setStyle(nameStyle);
			}
		} else {
			ItemStack stack = ItemRepository.getItemStack(id);
			if (stack != null) {
				return stack.getHoverName();
			}
		}
		return Component.literal(id);
	}

	/**
	 * Retrieves the set ID for a given piece ID.
	 *
	 * @param id the piece ID to search for
	 * @return the ID of the set that the piece belongs to, or null if not found
	 */
	protected static @Nullable String getSetID(String id) {
		for (Donation donation : MuseumItemCache.MUSEUM_DONATIONS) {
			for (ObjectObjectMutablePair<String, PriceData> set : donation.getSet()) {
				if (set.left().equals(id)) {
					return donation.getId();
				}
			}
		}
		return null;
	}

	protected static List<String> getPiecesBySetID(String donationId) {
		return MuseumItemCache.MUSEUM_DONATIONS.stream()
				.filter(d -> d.getId().equals(donationId))
				.flatMap(d -> d.getSet().stream())
				.map(ObjectObjectMutablePair::left)
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
		} else if (!lowercaseKey.matches(".*(armor|outfit|suit|tuxedo).*")) {
			formattedName.append("Armor");
		}

		return formattedName.toString().trim();
	}

	/**
	 * Checks if the given item ID represents an equipment piece.
	 */
	public static boolean isEquipment(String skyblockApiId) {
		String upperId = skyblockApiId.toUpperCase(Locale.ENGLISH);
		return EQUIPMENT_TYPES.stream().anyMatch(upperId::contains);
	}

	/**
	 * Formats a double value into a shortened human-readable format.
	 *
	 * @param value The number to format.
	 * @return A formatted string (e.g., "10M", "5K", "1.2B").
	 */
	public static String formatPrice(double value) {
		return Formatters.SHORT_FLOAT_NUMBERS.format(value).replace(".0", "");
	}
}
