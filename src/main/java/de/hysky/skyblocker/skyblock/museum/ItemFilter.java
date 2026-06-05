package de.hysky.skyblocker.skyblock.museum;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemFilter {
	private final List<String> categories = new ArrayList<>();
	private int filterIndex = 0;

	public void updateCategories() {
		categories.clear();
		this.categories.add("All");
		this.categories.addAll(MuseumItemCache.MUSEUM_CATEGORIES);
		this.categories.remove("special");
		filterIndex = Math.min(filterIndex, categories.size()-1);
	}

	// Method to cycle through filtering modes and apply the corresponding logic
	public void cycleFilterMode(List<Donation> items, List<Donation> filteredList) {
		if (categories.isEmpty()) return;
		filterIndex = (filterIndex + 1) % categories.size();
		applyFilter(items, filteredList);
	}

	public void applyFilter(List<Donation> items, List<Donation> filteredList) {
		filteredList.clear();
		if (filterIndex == 0) {
			filteredList.addAll(items);
		} else {
			items.stream().filter(d -> d.getCategory().equals(categories.get(filterIndex))).forEach(filteredList::add);
		}
	}

	public Tooltip getTooltip() {
		MutableComponent tooltip = Component.translatable("skyblocker.museum.hud.filter").append("\n\n").withStyle(ChatFormatting.GREEN);
		int i = 0;
		for (String category : categories) {
			String categoryName = category.length() < 2 ? category : category.substring(0, 1).toUpperCase(Locale.ENGLISH) + category.substring(1);
			tooltip.append(getFilterText(i, Component.literal(categoryName)));
			i += 1;
		}
		tooltip.append("\n").append(Component.translatable("skyblocker.museum.hud.filter.switch").withStyle(ChatFormatting.YELLOW));
		return Tooltip.create(tooltip);
	}

	private Component getFilterText(int index, Component category) {
		boolean isCurrent = index == filterIndex;
		return Component.literal((isCurrent ? "➤ " : "  ")).append(category).append("\n")
				.withStyle(isCurrent ? ChatFormatting.AQUA : ChatFormatting.GRAY);
	}
}
