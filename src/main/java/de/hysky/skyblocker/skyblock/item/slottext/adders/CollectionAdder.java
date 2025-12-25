package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CollectionAdder extends SimpleSlotTextAdder {
	private static final Pattern COLLECTION = Pattern.compile("^[\\w -]+ (?<level>[IVXLCDM]+)$");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"collection",
			"skyblocker.config.uiAndVisuals.slotText.collectionLevel"
			);

	public CollectionAdder() {
		super("^\\w+ Collections", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId > 53) return List.of();
		Matcher matcher = COLLECTION.matcher(stack.getHoverName().getString());
		if (matcher.matches()) {
			int level = RomanNumerals.romanToDecimal(matcher.group("level"));
			if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Progress to ")) != null) {
				return SlotText.bottomRightList(Component.literal(String.valueOf(level)).withColor(SlotText.CREAM));
			} else {
				return SlotText.bottomRightList(Component.literal(String.valueOf(level)).withColor(SlotText.GOLD));
			}
		}

		return List.of();
	}
}
