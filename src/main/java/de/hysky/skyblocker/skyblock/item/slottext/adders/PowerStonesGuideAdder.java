package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class PowerStonesGuideAdder extends SimpleSlotTextAdder {
	private static final Pattern LEARNED = Pattern.compile("Learned: (Yes|Not Yet) (?<symbol>[✖✔])");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"power_stones_guide",
			"skyblocker.config.uiAndVisuals.slotText.powerStonesGuide",
			"skyblocker.config.uiAndVisuals.slotText.powerStonesGuide.@Tooltip");

	public PowerStonesGuideAdder() {
		super("^Power Stones Guide", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		Matcher match = ItemUtils.getLoreLineIfMatch(stack, LEARNED);
		if (match == null) return List.of();
		String symbol = match.group("symbol");
		Component text = symbol.equals("✖")
				? Component.literal("✘").withColor(SlotText.LIGHT_RED)
				: Component.literal("✔").withColor(SlotText.LIGHT_GREEN);

		return SlotText.bottomRightList(text);
	}
}
