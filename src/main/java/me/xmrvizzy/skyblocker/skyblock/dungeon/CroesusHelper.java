package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.container.ColorHighlight;
import me.xmrvizzy.skyblocker.container.ContainerSolver;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CroesusHelper extends ContainerSolver {

    public CroesusHelper() { super("^Croesus$"); }

    @Override
    public boolean isEnabled() {
        return SkyblockerMod.getInstance().CONFIG.dungeons.croesusHelper();
    }

    @Override
    public List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> entry : slots.entrySet()) {
            ItemStack stack = entry.getValue();
            if (stack != null && stack.getNbt() != null && stack.getNbt().toString().contains("opened"))
                highlights.add(new ColorHighlight(entry.getKey(), GRAY_HIGHLIGHT));
        }
        return highlights;
    }
}
