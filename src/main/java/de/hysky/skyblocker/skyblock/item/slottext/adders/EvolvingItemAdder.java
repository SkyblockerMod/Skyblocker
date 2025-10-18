package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Evolving items are those that get upgraded after holding them in your inventory for a certain amount of time.
public class EvolvingItemAdder extends SimpleSlotTextAdder {
	private static final Pattern BONUS_PATTERN = Pattern.compile("\\+?([\\d.]+)");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"evolving_items",
			"skyblocker.config.uiAndVisuals.slotText.evolvingItems",
			"skyblocker.config.uiAndVisuals.slotText.evolvingItems.@Tooltip");

	public EvolvingItemAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		switch (stack.getSkyblockId()) {
			case "NEW_BOTTLE_OF_JYRRE", "DARK_CACAO_TRUFFLE", "DISCRITE", "MOBY_DUCK" -> {
				return actualLogic(stack, "Current Bonus: ");
			}
			case "TRAINING_WEIGHTS" -> {
				return actualLogic(stack, "Strength Gain: ");
			}
			// This is the old item called "Lost Bottle of Jyrre" now, this version of the item can't evolve
			// but its intelligence bonus similarly increases as it's held.
			// It's unobtainable but there are still some in the game.
			case "BOTTLE_OF_JYRRE" -> {
				return actualLogic(stack, "Intelligence Bonus: ");
			}
		}
		return List.of();
	}

	// This method was extracted to avoid duplicating the whole method multiple times with just 1 different string equality check.
	private List<SlotText> actualLogic(ItemStack stack, String equal) {
		List<Text> lore = ItemUtils.getLore(stack);
		if (lore.isEmpty()) return List.of();
		for (Text line : lore) {
			List<Text> siblings = line.getSiblings();
			if (siblings.size() < 2) continue;
			if (siblings.getFirst().getString().equals(equal)) {
				Text bonus = siblings.get(1);
				Matcher matcher = BONUS_PATTERN.matcher(bonus.getString());
				OptionalDouble result = RegexUtils.findDoubleFromMatcher(matcher);
				if (result.isPresent()) return SlotText.bottomLeftList(Text.literal(Formatters.DOUBLE_NUMBERS.format(result.getAsDouble())).setStyle(bonus.getStyle()));
			}
		}
		return List.of();
	}
}
