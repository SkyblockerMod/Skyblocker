package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
        Matcher statMatcher = ItemUtils.getLoreLineIfMatch(stack, STATHAS);
        Matcher unassignedMatcher = ItemUtils.getLoreLineIfMatch(stack, UNASSIGNEDPOINTS);

        if (stack.getName().getString().equals("Stats Tuning")) {
            if (unassignedMatcher == null) return List.of();
            String unassignedPoints = unassignedMatcher.group("points");
            return SlotText.bottomRightList(Text.literal(unassignedPoints).withColor(0xFFDDC1));
        }

        if (statMatcher == null) return List.of();
        String assignedPoints = statMatcher.group("points");
        if (assignedPoints.equals("0")) return List.of();
        return SlotText.bottomRightList(Text.literal(assignedPoints).withColor(0xFFDDC1));

    }
}
