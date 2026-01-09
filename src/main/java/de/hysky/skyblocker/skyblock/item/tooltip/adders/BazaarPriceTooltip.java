package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class BazaarPriceTooltip extends SimpleTooltipAdder {
	public BazaarPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		String skyblockApiId = stack.getSkyblockApiId();

		if (TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
			int count = Math.max(ItemUtils.getItemCountInSack(stack, stack.skyblocker$getLoreStrings()).orElse(ItemUtils.getItemCountInStash(lines.getFirst()).orElse(ItemUtils.getItemCountInSuperpairs(stack).orElse(stack.getCount()))), 1);

			BazaarProduct product = TooltipInfoType.BAZAAR.getData().get(skyblockApiId);
			lines.add(Component.literal(String.format("%-18s", "Bazaar Buy Price:"))
						.withStyle(ChatFormatting.GOLD)
						.append(product.buyPrice().isEmpty()
								? Component.literal("No data").withStyle(ChatFormatting.RED)
								: ItemTooltip.getCoinsMessage(product.buyPrice().getAsDouble(), count)));
			lines.add(Component.literal(String.format("%-19s", "Bazaar Sell Price:"))
						.withStyle(ChatFormatting.GOLD)
						.append(product.sellPrice().isEmpty()
								? Component.literal("No data").withStyle(ChatFormatting.RED)
								: ItemTooltip.getCoinsMessage(product.sellPrice().getAsDouble(), count)));
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.BAZAAR.isTooltipEnabled();
	}
}
