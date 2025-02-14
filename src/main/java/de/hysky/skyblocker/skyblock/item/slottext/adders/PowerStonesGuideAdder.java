package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
        Matcher match = ItemUtils.getLoreLineIfMatch(stack, LEARNED);
        if (match == null) return List.of();
        String symbol = match.group("symbol");
        Text text = symbol.equals("✖")
                ? Text.literal("✖").withColor(0xFF7276)
                : Text.literal("✔").withColor(0x90ee90);

        return SlotText.bottomRightList(text);
    }
}
