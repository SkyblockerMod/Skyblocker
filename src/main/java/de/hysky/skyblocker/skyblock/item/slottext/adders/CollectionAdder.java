package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionAdder extends SlotTextAdder {
    private static final Pattern COLLECTION = Pattern.compile("^[\\w ]+ (?<level>[IVXLCDM]+)$");

    public CollectionAdder() {
        super("^\\w+ Collections");
    }

    @Override
    public @NotNull List<SlotText> getText(Slot slot) {
        final ItemStack stack = slot.getStack();
        Matcher matcher = COLLECTION.matcher(stack.getName().getString());
        if (matcher.matches()) {
            int level = RomanNumerals.romanToDecimal(matcher.group("level"));
            if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Progress to ")) != null) {
                return List.of(SlotText.bottomRight(Text.literal(String.valueOf(level)).withColor(0xFFDDC1)));
            } else {
                return List.of(SlotText.bottomRight(Text.literal(String.valueOf(level)).withColor(0xE5B80B)));
            }
        }
        return List.of();
    }
}
