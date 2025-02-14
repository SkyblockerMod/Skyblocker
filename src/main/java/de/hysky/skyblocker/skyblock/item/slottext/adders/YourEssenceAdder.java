package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YourEssenceAdder extends SimpleSlotTextAdder {
	private static final Pattern YOUR_ESSENCE = Pattern.compile("You currently own (?<essence>[\\d,]+)");
	private static final Pattern ESSENCE_GUIDE = Pattern.compile("Your \\w+ Essence: (?<essence>[\\d,]+)");
	public static final NumberFormat COMPACT_NUMBER_FORMATTER = NumberFormat.getCompactNumberInstance(Locale.CANADA, NumberFormat.Style.SHORT);

	static {
		COMPACT_NUMBER_FORMATTER.setMinimumFractionDigits(1);
	}
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"your_essence",
			"skyblocker.config.uiAndVisuals.slotText.yourEssence");

	public YourEssenceAdder() {
		super("^(?:Your Essence|Essence Guide)", CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (stack.getName().getString().contains("Essence")) {
			return essenceAmountMatcher(ItemUtils.getLore(stack)).<List<SlotText>>map(essenceAmountMatcher -> {
				String essenceAmount = essenceAmountMatcher.group("essence").replace(",", "");
				if (!essenceAmount.matches("-?\\d+")) return List.of();
				return SlotText.bottomRightList(Text.literal(COMPACT_NUMBER_FORMATTER.format(Integer.parseInt(essenceAmount))).withColor(0xFFDDC1));
			}).orElse(List.of());
		}
		return List.of();
	}

	@NotNull
	private Optional<Matcher> essenceAmountMatcher(List<Text> lore) {
		if (lore.isEmpty()) return Optional.empty();
		Matcher essenceAmountMatcher = YOUR_ESSENCE.matcher(lore.getFirst().getString());
		if (essenceAmountMatcher.find()) {
			return Optional.of(essenceAmountMatcher);
		}
		if (lore.size() < 3) return Optional.empty();
		essenceAmountMatcher = ESSENCE_GUIDE.matcher(lore.get(lore.size() - 3).getString());
		if ((essenceAmountMatcher).find()) {
			return Optional.of(essenceAmountMatcher);
		}
		return Optional.empty();
	}
}
