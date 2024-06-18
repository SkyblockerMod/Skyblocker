package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerStonesGuideAdder extends SlotTextAdder {
    private static final Pattern LEARNED = Pattern.compile("Learned: (Yes|Not Yet) (?<symbol>[✖✔])");

    public PowerStonesGuideAdder() {
        super("^Power Stones Guide");
    }

    @Override
    public @NotNull List<SlotText> getText(Slot slot) {
        final ItemStack stack = slot.getStack();

        Matcher match = ItemUtils.getLoreLineIfMatch(stack, LEARNED);
        if (match == null) return List.of();
        String symbol = match.group("symbol");
        Text text;
        if (symbol.equals("✖")) {
            text = Text.literal("✖").withColor(0xFF7276);
        } else {
            text = Text.literal("✔").withColor(0x90ee90);
        }

        return List.of(SlotText.bottomRight(text));
    }
}
