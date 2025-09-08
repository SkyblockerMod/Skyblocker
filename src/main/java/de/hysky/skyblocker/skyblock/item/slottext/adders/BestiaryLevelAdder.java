package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestiaryLevelAdder extends SimpleSlotTextAdder {
	//^[\w '-]+ (?<level>[IVXLCDM]+)$
	private static final Pattern BESTIARY = Pattern.compile("^[\\w '-]+ (?<level>[IVXLCDM]+)$");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"bestiary_level",
			"skyblocker.config.uiAndVisuals.slotText.bestiaryLevel"
	);

	public BestiaryLevelAdder() {
		//(?:\(\d+\/\d+\) )?(?:Bestiary|Fishing) ➜ .+
		super("(?:\\(\\d+\\/\\d+\\) )?(?:Bestiary|Fishing) ➜ .+", CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		//Ignore slots that cannot have bestiary texts
		if (!((slotId >= 10 && slotId <= 16) || (slotId >= 19 && slotId <= 25) || (slotId >= 28 && slotId <= 34) || (slotId >= 37 && slotId <= 43))) return List.of();
		//Ignore slots without an item or bestiaries that aren't unlocked
		if (stack.isEmpty() || stack.isOf(Items.GRAY_DYE)) return List.of();
		Matcher matcher = BESTIARY.matcher(stack.getName().getString());
		if (matcher.matches()) {
			int level = RomanNumerals.romanToDecimal(matcher.group("level"));
			if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Overall Progress: 100%")) != null) {
				return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(SlotText.GOLD));
			} else {
				return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM));
			}
		}

		return List.of();
	}
}
