package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StatsTuningAdder extends SimpleSlotTextAdder {
	private static final Pattern STATHAS = Pattern.compile("Stat has: (?<points>\\d+) (points|point)");
	private static final Pattern UNASSIGNEDPOINTS = Pattern.compile("Unassigned Points: (?<points>\\d+)!!!");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"stats_tuning",
			"skyblocker.config.uiAndVisuals.slotText.statsTuning",
			"skyblocker.config.uiAndVisuals.slotText.statsTuning.@Tooltip");

	public StatsTuningAdder() {
		super("^Stats Tuning", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		Matcher statMatcher = ItemUtils.getLoreLineIfMatch(stack, STATHAS);
		Matcher unassignedMatcher = ItemUtils.getLoreLineIfMatch(stack, UNASSIGNEDPOINTS);

		if (stack.getHoverName().getString().equals("Stats Tuning")) {
			if (unassignedMatcher == null) return List.of();
			String unassignedPoints = unassignedMatcher.group("points");
			return SlotText.bottomRightList(Component.literal(unassignedPoints).withColor(SlotText.CREAM));
		}

		if (statMatcher == null) return List.of();
		String assignedPoints = statMatcher.group("points");
		if (assignedPoints.equals("0")) return List.of();
		return SlotText.bottomRightList(Component.literal(assignedPoints).withColor(SlotText.CREAM));

	}
}
