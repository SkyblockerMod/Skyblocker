package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YourEssenceAdder extends SlotTextAdder {
    private static final Pattern ESSENCE = Pattern.compile("You currently own (?<essence>[\\d,]+)");

    public YourEssenceAdder() {
        super("^Your Essence");
    }

    @Override
    public @NotNull List<SlotText> getText(@NotNull ItemStack stack, int slotId) {
        String name = stack.getName().getString();
        if (name.contains("Essence")) {
            List<Text> lore = ItemUtils.getLore(stack);
            if (lore.isEmpty()) return List.of();
            String essenceAmountText = lore.getFirst().getString();

            Matcher essenceAmountMatcher = ESSENCE.matcher(essenceAmountText);
            if (essenceAmountMatcher.find()) {
                String essenceAmount = essenceAmountMatcher.group("essence").replace(",", "");
                if (!essenceAmount.matches("-?\\d+")) return List.of();
                NumberFormat NUMBER_FORMATTER_S = NumberFormat.getCompactNumberInstance(Locale.CANADA, NumberFormat.Style.SHORT);
                NUMBER_FORMATTER_S.setMinimumFractionDigits(1);
                int amount = Integer.parseInt(essenceAmount);

                return List.of(SlotText.bottomRight(Text.literal(NUMBER_FORMATTER_S.format(amount)).withColor(0xFFDDC1)));
            }
        }
        return List.of();
    }
}