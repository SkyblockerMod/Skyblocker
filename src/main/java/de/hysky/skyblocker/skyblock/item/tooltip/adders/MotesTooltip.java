package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MotesTooltip extends SimpleTooltipAdder {
	public MotesTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.MOTES.hasOrNullWarning(internalID)) {
			lines.add(Component.literal(String.format("%-20s", "Motes Price:"))
						.withStyle(ChatFormatting.LIGHT_PURPLE)
						.append(getMotesMessage(TooltipInfoType.MOTES.getData().getInt(internalID), stack.getCount())));
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.MOTES.isTooltipEnabled();
	}

	private static Component getMotesMessage(int price, int count) {
		float motesMultiplier = SkyblockerConfigManager.get().otherLocations.rift.mcGrubberStacks * 0.05f + 1;

		// Calculate the total price
		int totalPrice = price * count;
		String totalPriceString = String.format(Locale.ENGLISH, "%1$,.1f", totalPrice * motesMultiplier);

		// If count is 1, return a simple message
		if (count == 1) {
			return Component.literal(totalPriceString.replace(".0", "") + " Motes").withStyle(ChatFormatting.DARK_AQUA);
		}

		// If count is greater than 1, include the "each" information
		String eachPriceString = String.format(Locale.ENGLISH, "%1$,.1f", price * motesMultiplier);
		MutableComponent message = Component.literal(totalPriceString.replace(".0", "") + " Motes ").withStyle(ChatFormatting.DARK_AQUA);
		message.append(Component.literal("(" + eachPriceString.replace(".0", "") + " each)").withStyle(ChatFormatting.GRAY));

		return message;
	}
}
