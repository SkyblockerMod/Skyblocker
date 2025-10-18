package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommissionHighlight extends SimpleContainerSolver {
    public CommissionHighlight() {
        super("^Commissions$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().mining.commissionHighlight;
    }

    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            ItemStack stack = entry.getValue();
            if (stack != null && stack.contains(DataComponentTypes.LORE)) {
                if (ItemUtils.getLoreLineIf(stack, s -> s.contains("COMPLETED")) != null) {
                    highlights.add(ColorHighlight.green(entry.getIntKey()));
                }
            }
        }
        return highlights;
    }
}
