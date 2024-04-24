package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StartsWithTerminal extends ContainerSolver {
    public StartsWithTerminal() {
        super("^What starts with: '([A-Z])'\\?$");
    }

    @Override
    protected boolean isEnabled() {
        return SkyblockerConfigManager.get().locations.dungeons.terminals.solveStartsWith;
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        trimEdges(slots, 6);
        String prefix = groups[0];
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> slot : slots.entrySet()) {
            ItemStack stack = slot.getValue();
            if (!stack.hasGlint() && stack.getName().getString().startsWith(prefix)) {
                highlights.add(ColorHighlight.green(slot.getKey()));
            }
        }
        return highlights;
    }
}