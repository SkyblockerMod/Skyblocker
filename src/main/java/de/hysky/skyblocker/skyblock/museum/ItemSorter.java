package de.hysky.skyblocker.skyblock.museum;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemSorter {
	private SortMode currentSortMode = SortMode.LowestBIN;

	private static void sortByLowestBIN(List<Donation> donations) {
		donations.forEach(donation -> setEffectivePrices(donation, false));
		donations.sort(ItemSorter::compareEffectivePrices);
	}

	private static void sortByCraftCost(List<Donation> donations) {
		donations.forEach(donation -> setEffectivePrices(donation, true));
		donations.sort(ItemSorter::compareEffectivePrices);
	}

	private static void sortByXpPerCoin(List<Donation> donations) {
		donations.forEach(donation -> setEffectivePrices(donation, true));
		donations.sort(ItemSorter::compareXpPerCoin);
	}

	// Set effective prices for the donation and its armor set pieces
	public static void setEffectivePrices(Donation donation, boolean useCraftCost) {
		if (donation.isArmorSet()) {
			donation.getSet().forEach(piece ->
					piece.setEffectivePrice(resolveEffectivePrice(piece.getPrice(), useCraftCost ? piece.getCraftCost() : 0))
			);
		}
		donation.setEffectivePrice(resolveEffectivePrice(donation.getPrice(), useCraftCost ? donation.getCraftCost() : 0));
	}

	private static int compareEffectivePrices(Donation a, Donation b) {
		double priceA = a.getEffectivePrice();
		double priceB = b.getEffectivePrice();

		if (priceA <= 0 && priceB <= 0) return 0; // Both prices are invalid
		if (priceA <= 0) return 1; // Move invalid price to the end
		if (priceB <= 0) return -1; // Move invalid price to the end
		return Double.compare(priceA, priceB); // Compare valid prices in ascending order
	}

	// Resolve the effective price based on price and craft cost
	private static double resolveEffectivePrice(double price, double craftCost) {
		if (price > 0 && craftCost > 0) {
			return Math.min(price, craftCost);
		}
		return price > 0 ? price : craftCost; // Choose whichever is valid
	}

	// Comparison logic for XP per Coin
	private static int compareXpPerCoin(Donation a, Donation b) {
		double xpPerCoinA = calculateXpPerCoin(a);
		double xpPerCoinB = calculateXpPerCoin(b);

		// If both ratios are 0, consider them equal
		if (xpPerCoinA == 0 && xpPerCoinB == 0) return 0;

		// Move items with 0 XP per Coin to the end
		if (xpPerCoinA == 0) return 1;
		if (xpPerCoinB == 0) return -1;

		// Compare XP per Coin in descending order
		return Double.compare(xpPerCoinB, xpPerCoinA);
	}

	// Helper method to calculate XP per Coin
	private static double calculateXpPerCoin(Donation donation) {
		double effectivePrice = donation.getEffectivePrice();
		return effectivePrice > 0 ? donation.getXp() / effectivePrice : 0;
	}

	// Method to cycle through sorting modes and apply the corresponding logic
	public void cycleSortMode(List<Donation> donations) {
		// Cycle to the next sorting mode
		currentSortMode = SortMode.values()[(currentSortMode.ordinal() + 1) % SortMode.values().length];
		// Apply the sorting logic for the current mode
		currentSortMode.applySort(donations);
	}

	public void applySort(List<Donation> donations) {
		currentSortMode.applySort(donations);
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
				.append(getSortText(SortMode.XpPerCoin))
				.append(Text.literal("\nClick to switch sort!").formatted(Formatting.YELLOW));
		return Tooltip.of(tooltip);
	}

	private Text getSortText(SortMode mode) {
		boolean isCurrent = mode == currentSortMode;
		return Text.literal((isCurrent ? "➤ " : "  ") + mode.getDisplayName() + "\n")
				.formatted(isCurrent ? Formatting.AQUA : Formatting.GRAY);
	}

	public enum SortMode {
		LowestBIN(new ItemStack(Items.GOLD_INGOT), ItemSorter::sortByLowestBIN, "Lowest BIN"),
		CraftCost(new ItemStack(Items.CRAFTING_TABLE), ItemSorter::sortByCraftCost, "Craft Cost"),
		XpPerCoin(new ItemStack(Items.EXPERIENCE_BOTTLE), ItemSorter::sortByXpPerCoin, "Xp Per Coin");

		private final ItemStack associatedItem;
		private final SortFunction sortFunction;
		private final String displayName;

		SortMode(ItemStack item, SortFunction function, String displayName) {
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

		public void applySort(List<Donation> donations) {
			sortFunction.sort(donations);
		}
	}

	@FunctionalInterface
	public interface SortFunction {
		void sort(List<Donation> donations);
	}
}
