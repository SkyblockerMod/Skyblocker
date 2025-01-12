package de.hysky.skyblocker.skyblock.item.slottext.adders;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class HotmPerkLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"hotm_perk_level",
			"skyblocker.config.uiAndVisuals.slotText.hotmPerkLevel");
	private static final Pattern LEVEL = Pattern.compile("Level (?<level>\\d+)\\/(?<max>\\d+)");

	public HotmPerkLevelAdder() {
		super("^Heart of the Mountain$", CONFIG_INFORMATION);
	}

	@Override
	@NotNull
	public List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slotId >= 0 && slotId <= 53 && !stack.isOf(Items.COAL)) {
			List<Text> lore = ItemUtils.getLore(stack);

			if (!lore.isEmpty()) {
				String levelLine = lore.getFirst().getString();
				Matcher matcher = LEVEL.matcher(levelLine);

				if (matcher.matches()) {
					int level = Integer.parseInt(matcher.group("level"));
					int max = Integer.parseInt(matcher.group("max"));

					return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(level == max ? SlotText.GOLD : SlotText.CREAM));
				}
			}
		}

		return List.of();
	}
}
