package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CropMilestonesAdder extends SimpleSlotTextAdder {

	private static final ConfigInformation CONFIG_INFORMATION =
		new ConfigInformation(
			"crop_milestones",
			"skyblocker.config.uiAndVisuals.slotText.cropMilestones",
			"skyblocker.config.uiAndVisuals.slotText.cropMilestones.@Tooltip"
		);

	public CropMilestonesAdder() {
		super("^Crop Milestones", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(
		@Nullable Slot slot,
		ItemStack stack,
		int slotId
	) {
		String name = stack.getHoverName().getString();
		int lastSpace = name.lastIndexOf(' ');
		if (lastSpace == -1) return List.of();

		String number = name.substring(lastSpace + 1);
		if (!number.matches("\\d+")) return List.of();

		// TODO: Change the max milestone line
		boolean maxed = ItemUtils.getLoreLineIf(stack, s ->
			s.contains("Max milestone reached!")
		) != null;

		return SlotText.bottomRightList(
			Component.literal(number).withColor(
				maxed ? SlotText.GOLD : SlotText.CREAM
			)
		);
	}
}
