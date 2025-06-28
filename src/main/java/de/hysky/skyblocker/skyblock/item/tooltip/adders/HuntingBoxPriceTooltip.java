package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.hunting.Attribute;
import de.hysky.skyblocker.skyblock.hunting.Attributes;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HuntingBoxPriceTooltip extends SimpleTooltipAdder {
	public HuntingBoxPriceTooltip(int priority) {
		super("^Hunting Box$", priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		Attribute attribute = Attributes.getAttributeFromItemName(stack);

		if (attribute != null && TooltipInfoType.BAZAAR.hasOrNullWarning(attribute.apiId())) {
			int count = ItemUtils.getItemCountInHuntingBox(stack).orElse(1);
			BazaarProduct product = TooltipInfoType.BAZAAR.getData().get(attribute.apiId());
			boolean holdingShift = Screen.hasShiftDown();
			String shardText = count > 1 ? "Shards" : "Shard";

			lines.add(Text.literal(shardText + " Sell Price: ")
					  .formatted(Formatting.GOLD)
					  .append(product.sellPrice().isEmpty()
							  ? Text.literal("No data").formatted(Formatting.RED)
							  : ItemTooltip.getCoinsMessage(product.sellPrice().getAsDouble() * count, holdingShift ? count : 1, true)));
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().hunting.huntingBox.enabled;
	}
}
