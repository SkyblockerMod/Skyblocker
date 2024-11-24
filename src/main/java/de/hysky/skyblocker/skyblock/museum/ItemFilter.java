package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
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
		Text tooltip = Text.literal("Item Filter\n\n").formatted(Formatting.GREEN)
				.append(getFilterText(FilterMode.ALL))
				.append(getFilterText(FilterMode.WEAPONS))
				.append(getFilterText(FilterMode.ARMOR))
				.append(getFilterText(FilterMode.RARITIES))
				.append(Text.literal("\nClick to switch!").formatted(Formatting.YELLOW));
		return Tooltip.of(tooltip);
	}

	private Text getFilterText(FilterMode mode) {
		boolean isCurrent = mode == currentFilterMode;
		return Text.literal((isCurrent ? "➤ " : "  ") + mode.getDisplayName() + "\n")
				.formatted(isCurrent ? Formatting.AQUA : Formatting.GRAY);
	}

	public enum FilterMode {
		ALL(new ItemStack(Items.NETHER_STAR), ItemFilter::filterAll, "All"),
		WEAPONS(new ItemStack(Items.DIAMOND_SWORD), ItemFilter::filterWeapons, "Weapons"),
		ARMOR(new ItemStack(Items.DIAMOND_CHESTPLATE), ItemFilter::filterArmor, "Armor"),
		RARITIES(ItemRepository.getItemStack("JADERALD"), ItemFilter::filterRarities, "Rarities");

		private final ItemStack associatedItem;
		private final FilterFunction filterFunction;
		private final String displayName;

		FilterMode(ItemStack item, FilterFunction function, String displayName) {
			this.associatedItem = item;
			this.filterFunction = function;
			this.displayName = displayName;
		}

		public ItemStack getAssociatedItem() {
			return associatedItem;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void applyFilter(List<Donation> items, List<Donation> filteredList) {
			filteredList.clear();
			filteredList.addAll(filterFunction.filter(items));
		}
	}

	@FunctionalInterface
	public interface FilterFunction {
		List<Donation> filter(List<Donation> items);
	}
}
