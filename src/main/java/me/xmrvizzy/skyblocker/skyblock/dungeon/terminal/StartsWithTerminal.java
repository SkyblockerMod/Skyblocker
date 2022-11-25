package me.xmrvizzy.skyblocker.skyblock.dungeon.terminal;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.container.ColorHighlight;
import me.xmrvizzy.skyblocker.container.ContainerSolver;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StartsWithTerminal extends ContainerSolver {
    public StartsWithTerminal() {
        super("^What starts with: '([A-Z])'\\?$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerMod.getInstance().CONFIG.dungeons.terminals.solveStartsWith();
    }

    @Override
    public List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        trimEdges(slots, 6);
        String prefix = groups[0];
        List<ColorHighlight> highlights = new ArrayList<>();
        for(Map.Entry<Integer, ItemStack> slot : slots.entrySet()) {
            ItemStack stack = slot.getValue();
            if(!stack.hasEnchantments() && stack.getName().getString().startsWith(prefix))
                highlights.add(new ColorHighlight(slot.getKey(), GREEN_HIGHLIGHT));
        }
        return highlights;
    }
}