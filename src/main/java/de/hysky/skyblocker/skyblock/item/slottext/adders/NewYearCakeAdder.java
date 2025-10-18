package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NewYearCakeAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"new_year_cake_year",
			"skyblocker.config.uiAndVisuals.slotText.newYearCakeYear",
			"skyblocker.config.uiAndVisuals.slotText.newYearCakeYear.@Tooltip");

	public NewYearCakeAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.CAKE)) return List.of();
		int year = ItemUtils.getCustomData(stack).getInt("new_years_cake", 0);
		if (year <= 0) return List.of();
		return SlotText.bottomLeftList(Text.literal(String.valueOf(year)).withColor(SlotText.MID_BLUE));
	}
}
