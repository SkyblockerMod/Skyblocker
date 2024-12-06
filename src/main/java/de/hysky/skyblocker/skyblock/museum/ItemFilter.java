package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ItemFilter {
	private FilterMode currentFilterMode = FilterMode.ALL;

	// Filtering logic methods
	private static List<Donation> filterAll(List<Donation> donations) {
		return donations;
	}

	private static List<Donation> filterWeapons(List<Donation> donations) {
		return donations.stream()
				.filter(donation -> "weapons".equals(donation.getCategory()))
				.collect(Collectors.toList());
	}

	private static List<Donation> filterArmor(List<Donation> donations) {
		return donations.stream()
				.filter(donation -> "armor".equals(donation.getCategory()))
				.collect(Collectors.toList());
	}

	private static List<Donation> filterRarities(List<Donation> donations) {
		return donations.stream()
				.filter(donation -> "rarities".equals(donation.getCategory()))
				.collect(Collectors.toList());
	}

	// Method to cycle through filtering modes and apply the corresponding logic
	public void cycleFilterMode(List<Donation> items, List<Donation> filteredList) {
		// Cycle to the next filter mode
		currentFilterMode = FilterMode.values()[(currentFilterMode.ordinal() + 1) % FilterMode.values().length];
		// Apply the filtering logic for the current mode
		currentFilterMode.applyFilter(items, filteredList);
	}

	public void applyFilter(List<Donation> items, List<Donation> filteredList) {
		currentFilterMode.applyFilter(items, filteredList);
	}

	// Get the item associated with the current filter mode
	public ItemStack getCurrentFilterItem() {
		return currentFilterMode.getAssociatedItem();
	}

	public void resetFilter() {
		this.currentFilterMode = FilterMode.ALL;
	}

	public Tooltip getTooltip() {
		Text tooltip = Text.translatable("skyblocker.museum.hud.filter").append("\n\n").formatted(Formatting.GREEN)
				.append(getFilterText(FilterMode.ALL))
				.append(getFilterText(FilterMode.WEAPONS))
				.append(getFilterText(FilterMode.ARMOR))
				.append(getFilterText(FilterMode.RARITIES))
				.append("\n").append(Text.translatable("skyblocker.museum.hud.filter.switch").formatted(Formatting.YELLOW));
		return Tooltip.of(tooltip);
	}

	private Text getFilterText(FilterMode mode) {
		boolean isCurrent = mode == currentFilterMode;
		return Text.literal((isCurrent ? "➤ " : "  ")).append(mode.getDisplayName()).append("\n")
				.formatted(isCurrent ? Formatting.AQUA : Formatting.GRAY);
	}

	public enum FilterMode {
		ALL(new ItemStack(Items.NETHER_STAR), ItemFilter::filterAll, Text.translatable("skyblocker.museum.hud.filter.all")),
		WEAPONS(new ItemStack(Items.DIAMOND_SWORD), ItemFilter::filterWeapons, Text.translatable("skyblocker.museum.hud.filter.weapons")),
		ARMOR(new ItemStack(Items.DIAMOND_CHESTPLATE), ItemFilter::filterArmor, Text.translatable("skyblocker.museum.hud.filter.armor")),
		RARITIES(ItemRepository.getItemStack("JADERALD"), ItemFilter::filterRarities, Text.translatable("skyblocker.museum.hud.filter.rarities"));

		private final ItemStack associatedItem;
		private final UnaryOperator<List<Donation>> filterFunction;
		private final Text displayName;

		FilterMode(ItemStack item, UnaryOperator<List<Donation>> function, Text displayName) {
			this.associatedItem = item;
			this.filterFunction = function;
			this.displayName = displayName;
		}

		public ItemStack getAssociatedItem() {
			return associatedItem;
		}

		public Text getDisplayName() {
			return displayName;
		}

		public void applyFilter(List<Donation> items, List<Donation> filteredList) {
			filteredList.clear();
			filteredList.addAll(filterFunction.apply(items));
		}
	}
}
