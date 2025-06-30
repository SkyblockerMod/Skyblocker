package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestiaryLevelAdder extends SimpleSlotTextAdder {
	private static final Pattern BESTIARY = Pattern.compile("^[\\w -]+ (?<level>[IVXLCDM]+)$");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"bestiary_level",
			"skyblocker.config.uiAndVisuals.slotText.bestiaryLevel"
	);

	public BestiaryLevelAdder() {
		super("Bestiary ➜ .+", CONFIG_INFORMATION);
	}


	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slotId < 54) { // Prevent accidentally adding text to slots which also match the pattern in the inventory (like minions)
			Matcher matcher = BESTIARY.matcher(stack.getName().getString());
			if (matcher.matches()) {
				int level = RomanNumerals.romanToDecimal(matcher.group("level"));
				if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Overall Progress: 100%")) != null) {
					return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(SlotText.GOLD));
				} else {
					return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM));
				}
			}
		}
		return List.of();
	}
}
