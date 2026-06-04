package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class SkyblockGuideAdder extends SimpleSlotTextAdder {
	private static final Pattern GUIDE_PATTERN = Pattern.compile("^(?<symbol>[✖✔])\\s*(?<text>.+)");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"skyblock_guide",
			"skyblocker.config.uiAndVisuals.slotText.skyblockGuide");

	public SkyblockGuideAdder() {
		super("^(?:\\(\\d+\\/\\d+\\)\\s+)?Guide ➜ \\w+", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId < 18 || slotId > 44) return List.of();
		Matcher match = GUIDE_PATTERN.matcher(stack.getHoverName().getString());
		if (!match.matches()) return List.of();
		String symbol = match.group("symbol");
		Component text = symbol.equals("✖")
				? Component.literal("✘").withColor(SlotText.LIGHT_RED)
				: Component.literal("✔").withColor(SlotText.LIGHT_GREEN);

		return SlotText.bottomRightList(text);
	}
}
