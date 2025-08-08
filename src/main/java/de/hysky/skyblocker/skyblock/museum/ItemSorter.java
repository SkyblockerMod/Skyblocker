package de.hysky.skyblocker.skyblock.museum;

import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemSorter {
	private SortMode currentSortMode = SortMode.LOWEST_BIN;

	// Sorting logic
	private static final Consumer<List<Donation>> SORT_BY_LOWESTBIN = donations -> {
		donations.forEach(donation -> updateDonationData(donation, false));
		donations.sort(ItemSorter::compareEffectivePrices);
	};
	private static final Consumer<List<Donation>> SORT_BY_CRAFTCOST = donations -> {
		donations.forEach(donation -> updateDonationData(donation, true));
		donations.sort(ItemSorter::compareEffectivePrices);
	};
	private static final Consumer<List<Donation>> SORT_BY_XP_PER_COIN = donations -> {
		donations.forEach(donation -> updateDonationData(donation, true));
		donations.sort(ItemSorter::compareCoinsPerXP);
	};

	// Set effective prices for the donation and its armor set pieces
	public static void updateDonationData(Donation donation, boolean useCraftCost) {
		// Gather all donations that this one counts towards
		List<String> downgrades = donation.getDowngrades();
		ObjectDoublePair<String> discount = donation.getDiscount();
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
		double craftCost = discount != null ? rawCraftCost - discount.rightDouble() : rawCraftCost;
		double effectivePrice = useCraftCost
				? (craftCost > 0 ? (lBinPrice == 0 ? craftCost : Math.min(craftCost, lBinPrice)) : lBinPrice)
				: (lBinPrice > 0 ? lBinPrice : craftCost);
		double ratio = totalXP > 0 && effectivePrice > 0 ? effectivePrice / totalXP : 0;

		// Update donation with computed data
		if (donation.isSet())
			donation.getSet().forEach(pair -> pair.right().setEffectivePrice(effectivePrice == craftCost ? pair.right().getCraftCost() : pair.right().getLBinPrice()));

		donation.getPriceData().setEffectivePrice(effectivePrice);
		donation.setXpCoinsRatio(ratio);
		donation.setTotalXp(totalXP);
		donation.setCountsTowards(willCountFor.stream()
				.map(d -> ObjectIntPair.of(d.getId(), d.getXp()))
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

	public Tooltip getTooltip() {
		Text tooltip = Text.translatable("skyblocker.museum.hud.sorter").append("\n\n").formatted(Formatting.GREEN)
				.append(getSortText(SortMode.LOWEST_BIN))
				.append(getSortText(SortMode.CRAFT_COST))
				.append(getSortText(SortMode.COINS_PER_XP))
				.append("\n").append(Text.translatable("skyblocker.museum.hud.sorter.switch").formatted(Formatting.YELLOW));
		return Tooltip.of(tooltip);
	}

	private Text getSortText(SortMode mode) {
		boolean isCurrent = mode == currentSortMode;
		return Text.literal((isCurrent ? "➤ " : "  ")).append(mode.getDisplayName()).append("\n")
				.formatted(isCurrent ? Formatting.AQUA : Formatting.GRAY);
	}

	public enum SortMode {
		LOWEST_BIN(new ItemStack(Items.GOLD_INGOT), SORT_BY_LOWESTBIN, Text.translatable("skyblocker.museum.hud.sorter.lBin")),
		CRAFT_COST(new ItemStack(Items.CRAFTING_TABLE), SORT_BY_CRAFTCOST, Text.translatable("skyblocker.museum.hud.sorter.craftCost")),
		COINS_PER_XP(new ItemStack(Items.EXPERIENCE_BOTTLE), SORT_BY_XP_PER_COIN, Text.translatable("skyblocker.museum.hud.sorter.ratio"));

		private final ItemStack associatedItem;
		private final Consumer<List<Donation>> sortFunction;
		private final Text displayName;

		SortMode(ItemStack item, Consumer<List<Donation>> function, Text displayName) {
			this.associatedItem = item;
			this.sortFunction = function;
			this.displayName = displayName;
		}

		public ItemStack getAssociatedItem() {
			return associatedItem;
		}

		public Text getDisplayName() {
			return displayName;
		}

		public Consumer<List<Donation>> getSortFunction() {
			return sortFunction;
		}
	}
}
