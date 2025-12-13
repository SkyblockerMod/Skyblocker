package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

public class YourEssenceAdder extends SimpleSlotTextAdder {
	private static final Pattern YOUR_ESSENCE = Pattern.compile("You currently own (?<essence>[\\d,]+)");
	private static final Pattern ESSENCE_GUIDE = Pattern.compile("Your \\w+ Essence: (?<essence>[\\d,]+)");

	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"your_essence",
			"skyblocker.config.uiAndVisuals.slotText.yourEssence");

	public YourEssenceAdder() {
		super("^(?:Your Essence|Essence Guide)", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (stack.getName().getString().contains("Essence")) {
			return essenceAmountMatcher(stack.skyblocker$getLoreStrings()).<List<SlotText>>map(essenceAmountMatcher -> {
				String essenceAmount = essenceAmountMatcher.group("essence").replace(",", "");
				if (!essenceAmount.matches("-?\\d+")) return List.of();
				return SlotText.bottomRightList(Text.literal(Formatters.SHORT_FLOAT_NUMBERS.format(Integer.parseInt(essenceAmount))).withColor(SlotText.CREAM));
			}).orElse(List.of());
		}
		return List.of();
	}

	private Optional<Matcher> essenceAmountMatcher(List<String> lore) {
		if (lore.isEmpty()) return Optional.empty();
		Matcher essenceAmountMatcher = YOUR_ESSENCE.matcher(lore.getFirst());
		if (essenceAmountMatcher.find()) {
			return Optional.of(essenceAmountMatcher);
		}
		if (lore.size() < 3) return Optional.empty();
		essenceAmountMatcher = ESSENCE_GUIDE.matcher(lore.get(lore.size() - 3));
		if ((essenceAmountMatcher).find()) {
			return Optional.of(essenceAmountMatcher);
		}
		return Optional.empty();
	}
}
