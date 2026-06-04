package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class CroesusHelper extends SimpleContainerSolver {
	public static final CroesusHelper INSTANCE = new CroesusHelper();

	private CroesusHelper() {
		super("^Croesus|Vesuvius$");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().dungeons.croesusHelper;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack stack = entry.getValue();
			if (stack != null && stack.has(DataComponents.LORE)) {
				if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Opened Chest:")) != null) {
					highlights.add(ColorHighlight.gray(entry.getIntKey()));
				} else if (ItemUtils.getLoreLineIf(stack, s -> s.contains("No more chests to open!")) != null) {
					highlights.add(ColorHighlight.red(entry.getIntKey()));
				}
			}
		}
		return highlights;
	}
}
