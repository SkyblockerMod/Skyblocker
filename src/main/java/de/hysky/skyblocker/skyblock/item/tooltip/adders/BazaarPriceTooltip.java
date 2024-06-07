package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

public class BazaarPriceTooltip extends TooltipAdder {
	public static boolean bazaarExist = false;

	public BazaarPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(List<Text> lines, Slot focusedSlot) {
		bazaarExist = false;
		final ItemStack itemStack = focusedSlot.getStack();
		final String internalID = itemStack.getSkyblockId();
		if (internalID == null) return;
		String name = itemStack.getSkyblockName();
		if (name == null) return;

		if (name.startsWith("ISSHINY_")) name = "SHINY_" + internalID;

		if (TooltipInfoType.BAZAAR.isTooltipEnabledAndHasOrNullWarning(name)) {
			int amount;
			if (lines.get(1).getString().endsWith("Sack")) {
				//The amount is in the 2nd sibling of the 3rd line of the lore.                                              here V
				//Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
				String line = lines.get(3).getSiblings().get(1).getString().replace(",", "");
				amount = NumberUtils.isParsable(line) && !line.equals("0") ? Integer.parseInt(line) : itemStack.getCount();
			} else {
				amount = itemStack.getCount();
			}
			JsonObject getItem = TooltipInfoType.BAZAAR.getData().getAsJsonObject(name);
			lines.add(Text.literal(String.format("%-18s", "Bazaar buy Price:"))
			              .formatted(Formatting.GOLD)
			              .append(getItem.get("buyPrice").isJsonNull()
					             ? Text.literal("No data").formatted(Formatting.RED)
					              : ItemTooltip.getCoinsMessage(getItem.get("buyPrice").getAsDouble(), amount)));
			lines.add(Text.literal(String.format("%-19s", "Bazaar sell Price:"))
			              .formatted(Formatting.GOLD)
			              .append(getItem.get("sellPrice").isJsonNull()
					             ? Text.literal("No data").formatted(Formatting.RED)
					              : ItemTooltip.getCoinsMessage(getItem.get("sellPrice").getAsDouble(), amount)));
			bazaarExist = true;
		}
	}
}
