package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceShopAdder extends SlotTextAdder {
    private static final Pattern ESSENCELEVEL = Pattern.compile("^[\\w ]+ (?<level>[IVXLCDM]+)$");
    private static final Pattern UNLOCKED = Pattern.compile("UNLOCKED");
    private static final Pattern ESSENCE = Pattern.compile("Your \\w+ Essence: (?<essence>[\\d,]+)");

    public EssenceShopAdder() {
        super("Essence Shop");
    }

    @Override
    public @NotNull List<SlotText> getText(Slot slot) {
        final ItemStack stack = slot.getStack();

        Matcher essenceLevelMatcher = ESSENCELEVEL.matcher(stack.getName().getString());
        Matcher essenceAmountMatcher = ItemUtils.getLoreLineIfMatch(stack, ESSENCE);

        if (essenceLevelMatcher.matches()) {
            int level = RomanNumerals.romanToDecimal(essenceLevelMatcher.group("level"));
            Matcher unlockedMatcher = ItemUtils.getLoreLineIfMatch(stack, UNLOCKED);
            if (unlockedMatcher == null) {
                level -= 1;
            }
            return List.of(SlotText.bottomRight(Text.literal(String.valueOf(level)).withColor(0xFFDDC1)));
        }
        if (essenceAmountMatcher == null) return List.of();
        String essenceAmount = essenceAmountMatcher.group("essence").replace(",", "");
        if (!essenceAmount.matches("-?\\d+")) return List.of();
        NumberFormat NUMBER_FORMATTER_S = NumberFormat.getCompactNumberInstance(Locale.CANADA, NumberFormat.Style.SHORT);
        NUMBER_FORMATTER_S.setMinimumFractionDigits(1);
        int amount = Integer.parseInt(essenceAmount);

        return List.of(SlotText.bottomRight(Text.literal(NUMBER_FORMATTER_S.format(amount)).withColor(0xFFDDC1)));
    }
}
