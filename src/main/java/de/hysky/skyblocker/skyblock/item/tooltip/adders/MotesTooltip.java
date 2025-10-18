package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class MotesTooltip extends SimpleTooltipAdder {
	public MotesTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.MOTES.hasOrNullWarning(internalID)) {
			lines.add(Text.literal(String.format("%-20s", "Motes Price:"))
			              .formatted(Formatting.LIGHT_PURPLE)
			              .append(getMotesMessage(TooltipInfoType.MOTES.getData().getInt(internalID), stack.getCount())));
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.MOTES.isTooltipEnabled();
	}

	private static Text getMotesMessage(int price, int count) {
		float motesMultiplier = SkyblockerConfigManager.get().otherLocations.rift.mcGrubberStacks * 0.05f + 1;

		// Calculate the total price
		int totalPrice = price * count;
		String totalPriceString = String.format(Locale.ENGLISH, "%1$,.1f", totalPrice * motesMultiplier);

		// If count is 1, return a simple message
		if (count == 1) {
			return Text.literal(totalPriceString.replace(".0", "") + " Motes").formatted(Formatting.DARK_AQUA);
		}

		// If count is greater than 1, include the "each" information
		String eachPriceString = String.format(Locale.ENGLISH, "%1$,.1f", price * motesMultiplier);
		MutableText message = Text.literal(totalPriceString.replace(".0", "") + " Motes ").formatted(Formatting.DARK_AQUA);
		message.append(Text.literal("(" + eachPriceString.replace(".0", "") + " each)").formatted(Formatting.GRAY));

		return message;
	}
}
