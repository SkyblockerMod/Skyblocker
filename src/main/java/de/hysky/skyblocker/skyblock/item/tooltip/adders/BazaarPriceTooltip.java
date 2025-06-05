package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

public class BazaarPriceTooltip extends SimpleTooltipAdder {
	public BazaarPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		String skyblockApiId = stack.getSkyblockApiId();

		if (TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
			OptionalInt optCount = ItemUtils.getItemCountInSack(stack, lines);
			// This clamp is here to ensure that the tooltip doesn't show a useless price of 0 coins if the item count is 0.
			int count = optCount.isPresent() ? Math.max(optCount.getAsInt(), 1) : stack.getCount();

			BazaarProduct product = TooltipInfoType.BAZAAR.getData().get(skyblockApiId);
			lines.add(Text.literal(String.format("%-18s", "Bazaar buy Price:"))
						  .formatted(Formatting.GOLD)
						  .append(product.buyPrice().isEmpty()
								  ? Text.literal("No data").formatted(Formatting.RED)
								  : ItemTooltip.getCoinsMessage(product.buyPrice().getAsDouble(), count)));
			lines.add(Text.literal(String.format("%-19s", "Bazaar sell Price:"))
						  .formatted(Formatting.GOLD)
						  .append(product.sellPrice().isEmpty()
								  ? Text.literal("No data").formatted(Formatting.RED)
								  : ItemTooltip.getCoinsMessage(product.sellPrice().getAsDouble(), count)));
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.BAZAAR.isTooltipEnabled();
	}
}
