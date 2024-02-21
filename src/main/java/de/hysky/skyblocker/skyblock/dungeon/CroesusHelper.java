package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CroesusHelper extends ContainerSolver {

    public CroesusHelper() {
        super("^Croesus$");
    }

    @Override
    protected boolean isEnabled() {
        return SkyblockerConfigManager.get().locations.dungeons.croesusHelper;
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> entry : slots.entrySet()) {
            ItemStack stack = entry.getValue();
            if (stack != null && stack.getNbt() != null) {
                if (stack.getNbt().toString().contains("Opened Chest:")) {
                    highlights.add(ColorHighlight.gray(entry.getKey()));
                } else if (stack.getNbt().toString().contains("No more Chests to open!")) {
                    highlights.add(ColorHighlight.red(entry.getKey()));
                }
            }
        }
        return highlights;
    }
}
