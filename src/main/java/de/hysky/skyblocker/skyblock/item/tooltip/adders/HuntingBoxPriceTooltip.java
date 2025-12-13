package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.hunting.Attribute;
import de.hysky.skyblocker.skyblock.hunting.Attributes;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.HudHelper;

public class HuntingBoxPriceTooltip extends SimpleTooltipAdder {
	public HuntingBoxPriceTooltip(int priority) {
		super("^Hunting Box$", priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (focusedSlot == null || focusedSlot.index > 53) return;
		Attribute attribute = Attributes.getAttributeFromItemName(stack);

		if (attribute != null && TooltipInfoType.BAZAAR.hasOrNullWarning(attribute.apiId())) {
			int count = ItemUtils.getItemCountInHuntingBox(stack).orElse(1);
			BazaarProduct product = TooltipInfoType.BAZAAR.getData().get(attribute.apiId());
			boolean holdingShift = HudHelper.hasShiftDown();
			String shardText = count > 1 ? "Shards" : "Shard";

			lines.add(Component.literal(shardText + " Sell Price: ")
					.withStyle(ChatFormatting.GOLD)
					.append(product.sellPrice().isEmpty()
							? Component.literal("No data").withStyle(ChatFormatting.RED)
							: ItemTooltip.getCoinsMessage(product.sellPrice().getAsDouble() * count, holdingShift ? count : 1, true)));
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().hunting.huntingBox.enabled;
	}
}
