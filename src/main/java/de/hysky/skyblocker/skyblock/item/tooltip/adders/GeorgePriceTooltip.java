package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import org.jspecify.annotations.Nullable;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Adds a George sell price tooltip line to pet items.
 *
 * <p>George is an NPC in SkyBlock who buys pets at fixed prices based on pet type and rarity.
 * This tooltip adder displays the applicable George price so players can quickly evaluate
 * whether selling to George is worthwhile compared to the Bazaar or Auction House.
 *
 * <p>Price data is fetched from {@code https://hysky.de/api/georgeprices} and stored in
 * {@link TooltipInfoType#GEORGE}. Keys in that dataset follow the format
 * {@code RARITY_PETNAME} (e.g. {@code EPIC_SQUID}, {@code LEGENDARY_WOLF}).
 *
 * <p>Pet item stacks expose their Skyblock API ID via {@code getSkyblockApiId()} in the format
 * {@code LVL_1_RARITY_PETNAME}. The {@code LVL_1_} prefix is stripped before the lookup.
 *
 * @see TooltipInfoType#GEORGE
 * @see NpcPriceTooltip
 */
public class GeorgePriceTooltip extends SimpleTooltipAdder {

	/**
	 * @param priority the sort priority of this adder relative to other tooltip adders;
	 *                 lower values appear higher in the tooltip
	 */
	public GeorgePriceTooltip(int priority) {
		super(priority);
	}

	/**
	 * {@return whether the George price tooltip is enabled in config}
	 */
	@Override
	public boolean isEnabled() {
		return TooltipInfoType.GEORGE.isTooltipEnabled();
	}

	/**
	 * Appends a George sell price line to the tooltip of a pet item stack.
	 *
	 * <p>The method early-returns without adding a line if:
	 * <ul>
	 *   <li>the item is not a pet (Skyblock API ID does not start with {@code LVL_1_})</li>
	 *   <li>the George price dataset has not yet been downloaded</li>
	 *   <li>the pet has no entry in the George price dataset</li>
	 * </ul>
	 *
	 * @param focusedSlot the slot currently focused by the player, or {@code null} if none
	 * @param stack       the item stack whose tooltip is being built
	 * @param lines       the mutable list of tooltip lines to append to
	 */
	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (!stack.getSkyblockId().equals("PET")) return;

		if (TooltipInfoType.GEORGE.getData() == null) {
			ItemTooltip.nullWarning();
			return;
		}

		double price = TooltipInfoType.GEORGE.getData().getOrDefault(stack.getNeuName(), -1.0);
		if (price < 0) return;

		lines.add(Component.literal(String.format("%-21s", "George Sell Price:"))
				.withStyle(ChatFormatting.YELLOW)
				.append(ItemTooltip.getCoinsMessage(price, 1)));
	}
}
