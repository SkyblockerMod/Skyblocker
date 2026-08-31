package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.RomanNumerals;

public final class MinionWidget extends CollectionItemWidget {

	private MinionWidget(ItemStack icon, Component tierText, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(icon, tierText, tooltip, tooltipStyle);
	}

	public static MinionWidget create(String minionId, int maxTier, List<Integer> tiersCrafted, OptionalInt lowestUncraftedTier) {
		int displayedTier = lowestUncraftedTier.isPresent() ? lowestUncraftedTier.getAsInt() - 1 : maxTier;
		boolean isMaxTier = tiersCrafted.size() == maxTier;

		// The icon should be minimum of tier 1 minion since tier 0 does not exist
		ItemStack icon = ItemRepository.getItemStack(minionId + "_GENERATOR_" + Math.max(displayedTier, 1), Ico.BARRIER).getStackOrThrow();
		Component tierText = buildTierText(displayedTier, isMaxTier);
		List<Component> tooltip = buildTooltip(maxTier, tiersCrafted, isMaxTier, icon.getHoverName().getString());
		Identifier tooltipStyle = getTooltipType(isMaxTier);

		return new MinionWidget(icon, tierText, tooltip, tooltipStyle);
	}

	/// Creates a tooltip that looks similar to the one in /craftedgenerators.
	private static List<Component> buildTooltip(int maxTier, List<Integer> tiersCrafted, boolean isMaxTier, String name) {
		List<Component> tooltip = new ArrayList<>();

		int lastSpaceIndex = Math.clamp(name.lastIndexOf(' '), 0, name.length());
		ChatFormatting nameFormatting = isMaxTier ? ChatFormatting.GREEN : tiersCrafted.isEmpty() ? ChatFormatting.RED : ChatFormatting.YELLOW;
		Component nameText = Component.literal(name.substring(0, lastSpaceIndex)).withStyle(nameFormatting);
		tooltip.add(nameText);

		for (int i = 1; i <= maxTier; i++) {
			boolean craftedTier = tiersCrafted.contains(i);
			String craftedText = String.format(Locale.ENGLISH, "%s Tier %s", craftedTier ? "✔" : "✖", RomanNumerals.decimalToRoman(i));

			tooltip.add(Component.literal(craftedText).withStyle(craftedTier ? ChatFormatting.GREEN : ChatFormatting.RED));
		}

		return List.copyOf(tooltip);
	}
}
