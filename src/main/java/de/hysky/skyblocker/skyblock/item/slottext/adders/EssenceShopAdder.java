package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceShopAdder extends SimpleSlotTextAdder {
    private static final Pattern ESSENCELEVEL = Pattern.compile("^[\\w ]+ (?<level>[IVXLCDM]+)$");
    private static final Pattern UNLOCKED = Pattern.compile("UNLOCKED");
    private static final Pattern ESSENCE = Pattern.compile("Your \\w+ Essence: (?<essence>[\\d,]+)");

	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"essence_shop",
			"skyblocker.config.uiAndVisuals.slotText.essenceShop",
			"skyblocker.config.uiAndVisuals.slotText.essenceShop.@Tooltip"
	);

    public EssenceShopAdder() {
        super(".*Essence Shop", CONFIG_INFORMATION);
    }

    @Override
    public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
        Matcher essenceLevelMatcher = ESSENCELEVEL.matcher(stack.getName().getString());
        Matcher essenceAmountMatcher = ItemUtils.getLoreLineIfMatch(stack, ESSENCE);

        if (essenceLevelMatcher.matches()) {
            int level = RomanNumerals.romanToDecimal(essenceLevelMatcher.group("level"));
            Matcher unlockedMatcher = ItemUtils.getLoreLineIfMatch(stack, UNLOCKED);
            if (unlockedMatcher == null) {
                level -= 1;
            }
            return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(0xFFDDC1));
        }
        if (essenceAmountMatcher == null) return List.of();
        String essenceAmount = essenceAmountMatcher.group("essence").replace(",", "");
        if (!essenceAmount.matches("-?\\d+")) return List.of();

        return SlotText.bottomRightList(Text.literal(YourEssenceAdder.COMPACT_NUMBER_FORMATTER.format(Integer.parseInt(essenceAmount))).withColor(0xFFDDC1));
    }
}
