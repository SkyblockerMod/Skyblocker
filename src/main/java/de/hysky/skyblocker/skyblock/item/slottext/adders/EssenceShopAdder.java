package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId > 53) return List.of();
		Matcher essenceLevelMatcher = ESSENCELEVEL.matcher(stack.getHoverName().getString());
		Matcher essenceAmountMatcher = ItemUtils.getLoreLineIfMatch(stack, ESSENCE);

		if (essenceLevelMatcher.matches()) {
			int level = RomanNumerals.romanToDecimal(essenceLevelMatcher.group("level"));
			Matcher unlockedMatcher = ItemUtils.getLoreLineIfMatch(stack, UNLOCKED);
			if (unlockedMatcher == null) {
				level -= 1;
			}
			return SlotText.bottomRightList(Component.literal(String.valueOf(level)).withColor(SlotText.CREAM));
		}
		if (essenceAmountMatcher == null) return List.of();
		String essenceAmount = essenceAmountMatcher.group("essence").replace(",", "");
		if (!essenceAmount.matches("-?\\d+")) return List.of();

		return SlotText.bottomRightList(Component.literal(Formatters.SHORT_FLOAT_NUMBERS.format(Integer.parseInt(essenceAmount))).withColor(SlotText.CREAM));
	}
}
