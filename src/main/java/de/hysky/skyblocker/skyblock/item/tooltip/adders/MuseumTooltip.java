package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.museum.MuseumItemCache;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MuseumTooltip extends SimpleTooltipAdder {
	public MuseumTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.MUSEUM.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.MUSEUM.hasOrNullWarning(internalID)) {
			String itemCategory = TooltipInfoType.MUSEUM.getData().get(internalID);
			String format = "%-20s";

			//Special case the special category so that it doesn't always display not donated
			if (itemCategory.equals("Special")) {
				lines.add(Component.literal(String.format(format, "Museum: (" + itemCategory + ")"))
							.withStyle(ChatFormatting.LIGHT_PURPLE));
			} else {
				boolean isInMuseum = MuseumItemCache.hasItemInMuseum(internalID);

				ChatFormatting donatedIndicatorFormatting = isInMuseum ? ChatFormatting.GREEN : ChatFormatting.RED;

				lines.add(Component.literal(String.format(format, "Museum (" + itemCategory + "):"))
							.withStyle(ChatFormatting.LIGHT_PURPLE)
							.append(Component.literal(isInMuseum ? "✔" : "✖").withStyle(donatedIndicatorFormatting, ChatFormatting.BOLD))
							.append(Component.literal(isInMuseum ? " Donated" : " Not Donated").withStyle(donatedIndicatorFormatting)));
			}
		}
	}
}
