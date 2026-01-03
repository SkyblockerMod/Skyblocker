package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class NewYearCakeAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"new_year_cake_year",
			"skyblocker.config.uiAndVisuals.slotText.newYearCakeYear",
			"skyblocker.config.uiAndVisuals.slotText.newYearCakeYear.@Tooltip");

	public NewYearCakeAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.is(Items.CAKE)) return List.of();
		int year = ItemUtils.getCustomData(stack).getIntOr("new_years_cake", 0);
		if (year <= 0) return List.of();
		return SlotText.bottomLeftList(Component.literal(String.valueOf(year)).withColor(SlotText.MID_BLUE));
	}
}
