package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BazaarPriceTooltip extends SimpleTooltipAdder {
    public BazaarPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
        String skyblockApiId = stack.getSkyblockApiId();

		if (TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
			int count;
			if (lines.size() >= 4 && lines.get(3).getSiblings().size() >= 2 && lines.get(1).getString().endsWith("Sack")) {
				//The count is in the 2nd sibling of the 3rd line of the lore.                                              here V
				//Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
				String line = lines.get(3).getSiblings().get(1).getString().replace(",", "");
				count = NumberUtils.isParsable(line) && !line.equals("0") ? Integer.parseInt(line) : stack.getCount();
			} else {
				count = stack.getCount();
			}
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
