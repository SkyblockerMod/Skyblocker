package de.hysky.skyblocker.skyblock.museum;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemSorter {
	// Sorting logic
	private static final Consumer<List<Donation>> sortByLowestBIN = donations -> {
		donations.forEach(donation -> updateDonationData(donation, false));
		donations.sort(ItemSorter::compareEffectivePrices);
	};
	private static final Consumer<List<Donation>> sortByCraftCost = donations -> {
		donations.forEach(donation -> updateDonationData(donation, true));
		donations.sort(ItemSorter::compareEffectivePrices);
	};
	private static final Consumer<List<Donation>> sortByXpPerCoin = donations -> {
		donations.forEach(donation -> updateDonationData(donation, true));
		donations.sort(ItemSorter::compareCoinsPerXP);
	};
	private SortMode currentSortMode = SortMode.LowestBIN;

	// Set effective prices for the donation and its armor set pieces
	public static void updateDonationData(Donation donation, boolean useCraftCost) {
		// Gather all donations that this one counts towards
		List<String> downgrades = donation.getDowngrades();
		Pair<String, Double> discount = donation.getDiscount();
		List<Donation> willCountFor = downgrades.stream()
				.map(MuseumManager::getDonation)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		willCountFor.addFirst(donation); // Ensure donation itself is part of the list

		// Calculate cumulative XP
		int totalXP = willCountFor.stream().mapToInt(Donation::getXp).sum();

		// Calculate effective prices
		double lBinPrice = donation.getPriceData().getLBinPrice();
		double rawCraftCost = donation.isCraftable() ? donation.getPriceData().getCraftCost() : 0;
		double craftCost = discount != null ? rawCraftCost - discount.getRight() : rawCraftCost;
		double effectivePrice = useCraftCost
				? (craftCost > 0 ? (lBinPrice == 0 ? craftCost : Math.min(craftCost, lBinPrice)) : lBinPrice)
				: (lBinPrice > 0 ? lBinPrice : craftCost);
		double ratio = totalXP > 0 && effectivePrice > 0 ? effectivePrice / totalXP : 0;

		// Update donation with computed data
		if (donation.isSet()) donation.getSet().forEach(pair -> pair.getRight().setEffectivePrice(effectivePrice == craftCost ? pair.getRight().getCraftCost() : pair.getRight().getLBinPrice()));
		donation.getPriceData().setEffectivePrice(effectivePrice);
		donation.setXpCoinsRatio(ratio);
		donation.setTotalXp(totalXP);
		donation.setCountsTowards(willCountFor.stream()
				.map(d -> new Pair<>(d.getId(), d.getXp()))
				.toList());
	}

	private static int compareEffectivePrices(Donation a, Donation b) {
		double priceA = a.getPriceData().getEffectivePrice();
		double priceB = b.getPriceData().getEffectivePrice();

		if (priceA <= 0 && priceB <= 0) {
			// Both prices are invalid, sort by XP descending
			return Integer.compare(b.getTotalXp(), a.getTotalXp());
		}
		if (priceA <= 0) return 1; // Move invalid price to the end
		if (priceB <= 0) return -1; // Move invalid price to the end

		// Compare valid prices in ascending order
		return Double.compare(priceA, priceB);
	}

	private static int compareCoinsPerXP(Donation a, Donation b) {
		double xpPerCoinA = a.getXpCoinsRatio();
		double xpPerCoinB = b.getXpCoinsRatio();

		if (xpPerCoinA == 0 && xpPerCoinB == 0) {
			// Both ratios are 0, sort by XP descending
			return Integer.compare(b.getTotalXp(), a.getTotalXp());
		}
		if (xpPerCoinA == 0) return 1; // Move items with 0 XP/coin to the end
		if (xpPerCoinB == 0) return -1; // Move items with 0 XP/coin to the end

		// Compare XP/coin ratios in descending order
		return Double.compare(xpPerCoinA, xpPerCoinB);
	}

	// Method to cycle through sorting modes and apply the corresponding logic
	public void cycleSortMode(List<Donation> donations) {
		// Cycle to the next sorting mode
		currentSortMode = SortMode.values()[(currentSortMode.ordinal() + 1) % SortMode.values().length];
		// Apply the sorting logic for the current mode
		applySort(donations);
	}

	public void applySort(List<Donation> donations) {
		currentSortMode.getSortFunction().accept(donations);
	}

	// Get the item associated with the current filter mode
	public ItemStack getCurrentSortingItem() {
		return currentSortMode.getAssociatedItem();
	}

	public void resetSorting() {
		this.currentSortMode = SortMode.LowestBIN;
	}

	public Tooltip getTooltip() {
		Text tooltip = Text.literal("Item Sort\n\n").formatted(Formatting.GREEN)
				.append(getSortText(SortMode.LowestBIN))
				.append(getSortText(SortMode.CraftCost))
				.append(getSortText(SortMode.CoinsPerXP))
				.append(Text.literal("\nClick to switch sort!").formatted(Formatting.YELLOW));
		return Tooltip.of(tooltip);
	}

	private Text getSortText(SortMode mode) {
		boolean isCurrent = mode == currentSortMode;
		return Text.literal((isCurrent ? "➤ " : "  ") + mode.getDisplayName() + "\n")
				.formatted(isCurrent ? Formatting.AQUA : Formatting.GRAY);
	}

	public enum SortMode {
		LowestBIN(new ItemStack(Items.GOLD_INGOT), sortByLowestBIN, "Lowest BIN"),
		CraftCost(new ItemStack(Items.CRAFTING_TABLE), sortByCraftCost, "Craft Cost"),
		CoinsPerXP(new ItemStack(Items.EXPERIENCE_BOTTLE), sortByXpPerCoin, "Coins/XP Ratio");

		private final ItemStack associatedItem;
		private final Consumer<List<Donation>> sortFunction;
		private final String displayName;

		SortMode(ItemStack item, Consumer<List<Donation>> function, String displayName) {
			this.associatedItem = item;
			this.sortFunction = function;
			this.displayName = displayName;
		}

		public ItemStack getAssociatedItem() {
			return associatedItem;
		}

		public String getDisplayName() {
			return displayName;
		}

		public Consumer<List<Donation>> getSortFunction() {
			return sortFunction;
		}
	}
}
