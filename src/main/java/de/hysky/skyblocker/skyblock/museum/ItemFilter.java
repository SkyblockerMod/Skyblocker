package de.hysky.skyblocker.skyblock.museum;

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
	private static final UnaryOperator<List<Donation>> FILTER_ALL = donations -> donations;
	private static final UnaryOperator<List<Donation>> FILTER_WEAPONS = donations ->
			donations.stream().filter(d -> "weapons".equals(d.getCategory())).collect(Collectors.toList());
	private static final UnaryOperator<List<Donation>> FILTER_ARMOR = donations ->
			donations.stream().filter(d -> "armor".equals(d.getCategory())).collect(Collectors.toList());
	private static final UnaryOperator<List<Donation>> FILTER_RARITIES = donations ->
			donations.stream().filter(d -> "rarities".equals(d.getCategory())).collect(Collectors.toList());

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
		ALL(new ItemStack(Items.NETHER_STAR), FILTER_ALL, Text.translatable("skyblocker.museum.hud.filter.all")),
		WEAPONS(new ItemStack(Items.DIAMOND_SWORD), FILTER_WEAPONS, Text.translatable("skyblocker.museum.hud.filter.weapons")),
		ARMOR(new ItemStack(Items.DIAMOND_CHESTPLATE), FILTER_ARMOR, Text.translatable("skyblocker.museum.hud.filter.armor")),
		RARITIES(new ItemStack(Items.EMERALD), FILTER_RARITIES, Text.translatable("skyblocker.museum.hud.filter.rarities"));

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
