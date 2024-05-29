package de.hysky.skyblocker.utils.tooltip;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BazaarPriceTooltip extends TooltipAdder {
	public static boolean bazaarExist = false;

	protected BazaarPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(List<Text> lore, Slot focusedSlot) {
		bazaarExist = false;
		final ItemStack itemStack = focusedSlot.getStack();
		final String internalID = ItemTooltip.getInternalNameFromNBT(itemStack, true);
		if (internalID == null) return;
		String name = ItemTooltip.getInternalNameFromNBT(itemStack, false);
		if (name == null) return;

		if (name.startsWith("ISSHINY_")) name = "SHINY_" + internalID;

		if (TooltipInfoType.BAZAAR.isTooltipEnabledAndHasOrNullWarning(name)) {
			JsonObject getItem = TooltipInfoType.BAZAAR.getData().getAsJsonObject(name);
			lore.add(Text.literal(String.format("%-18s", "Bazaar buy Price:"))
			             .formatted(Formatting.GOLD)
			             .append(getItem.get("buyPrice").isJsonNull()
					             ? Text.literal("No data").formatted(Formatting.RED)
					             : ItemTooltip.getCoinsMessage(getItem.get("buyPrice").getAsDouble(), itemStack.getCount())));
			lore.add(Text.literal(String.format("%-19s", "Bazaar sell Price:"))
			             .formatted(Formatting.GOLD)
			             .append(getItem.get("sellPrice").isJsonNull()
					             ? Text.literal("No data").formatted(Formatting.RED)
					             : ItemTooltip.getCoinsMessage(getItem.get("sellPrice").getAsDouble(), itemStack.getCount())));
			bazaarExist = true;
		}
	}
}
