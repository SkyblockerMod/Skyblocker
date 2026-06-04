package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChipLevelAdder extends SimpleSlotTextAdder {
	private static final Pattern LEVEL = Pattern.compile("Level (?<level>\\d+)/?(?<max>\\d+)?");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"chip_level",
			"skyblocker.config.uiAndVisuals.slotText.chipLevel",
			"skyblocker.config.uiAndVisuals.slotText.chipLevel.@tooltip"
	);

	public ChipLevelAdder() {
		super("^Manage Chips$", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId < 18 || slotId > 35 || stack.is(Items.GRAY_DYE)) return List.of();

		List<String> lore = stack.skyblocker$getLoreStrings();
		if (lore.isEmpty()) return List.of();
		String levelLine = lore.getFirst();
		Matcher matcher = LEVEL.matcher(levelLine);
		if (!matcher.matches()) return List.of();

		String level = matcher.group("level");
		String max = matcher.group("max");

		int color = SlotText.CREAM;
		if (Objects.equals(level, max)) {
			switch (max) {
				case "10" -> color = 0x1D8DE8;
				case "15" -> color = 0xA40BE2;
				case "20" -> color = SlotText.GOLD;
			}
		}

		return SlotText.bottomRightList(Component.literal(level).withColor(color));
	}

}
